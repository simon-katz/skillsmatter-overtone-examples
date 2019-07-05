(ns skillsmatter-overtone-examples.rhythms
  (:require [overtone.live :as o]
            [skillsmatter-overtone-examples.bug-fixes]
            [skillsmatter-overtone-examples.utils :as u]))

;; First, we'll define some percussive instruments

;; this high hat instrument takes a white noise generator and shapes
;; it with a percussive envelope

(o/definst hat [volume 1.0]
  (let [src (o/white-noise)
        env (o/env-gen (o/perc 0.001 0.3) :action o/FREE)]
    (* volume 1 src env)))

(comment
  (u/••• (hat))
  )

;; sampled kick drum
;; from http://www.freesound.org/people/opm/sounds/2086/
;; the overtone freesound API allows you to download freesounds samples
;; by id (2086 in this case)

(def kick (o/sample (o/freesound-path 2086)))

(comment
  (u/••• (kick))
  )

;; we can schedule beats for the future with the at macro:

(comment
  (u/••• (o/at (+ 1000 (o/now)) (kick)))
  )

;; ...and chain multiple beats together with a do form:

(comment
  (u/•••
   (let
       [time (o/now)]
     (o/at (+    0 time) (kick) )
     (o/at (+  400 time) (hat)  )
     (o/at (+  800 time) (kick) )
     (o/at (+ 1200 time) (hat)  )))
  )

;; to repeat, we use the apply-at macro to schedule a recursive call
;; for the future

(defn loop-beats [time]
  (o/at (+    0 time) (kick) )
  (o/at (+  400 time) (hat)  )
  (o/at (+  800 time) (kick) )
  (o/at (+ 1200 time) (hat)  )
  (o/apply-at (+ 1600 time) loop-beats (+ 1600 time) []))

(comment
  (u/••• (loop-beats (o/now)))
  (o/stop)
  )

;; rather than thinking in terms of milliseconds, it's useful to think
;; in terms of beats. We can create a metronome to help with this. A
;; metronome counts beats over time. Here's a metronome at 180 beats
;; per minute (bpm):

(defonce metro (o/metronome 240))

;; we use it as follows:
(comment
  (metro)  ; current beat number
  (metro 3) ; timestamp of beat number 3
  )
;; if we rewrite loop-beats using a metronome, it would look like
;; this:

(defn metro-beats [m beat-num]
  (o/at (m (+ 0 beat-num)) (kick))
  (o/at (m (+ 1 beat-num)) (hat))
  (o/at (m (+ 2 beat-num)) (kick))
  (o/at (m (+ 3 beat-num)) (hat))
  (o/apply-at (m (+ 4 beat-num)) metro-beats m (+ 4 beat-num) [])
  )

(comment
  (u/••• (metro-beats metro (metro)))
  ;; because we're using a metronome, we can change the speed:
  (metro :bpm 180) ;slower
  (metro :bpm 300) ;faster
  (o/stop)
  )


;; a more complex rhythm

(defn weak-hat []
  (hat 0.3))

(defn phat-beats [m beat-num]
  (o/at (m (+ 0 beat-num)) (kick) (weak-hat))
  (o/at (m (+ 1 beat-num)) (kick))
  (o/at (m (+ 2 beat-num))        (hat))
  (o/at (m (+ 3 beat-num)) (kick) (weak-hat))
  (o/at (m (+ 4 beat-num)) (kick) (weak-hat))
  (o/at (m (+ 4.5 beat-num)) (kick))
  (o/at (m (+ 5 beat-num)) (kick))
  (o/at (m (+ 6 beat-num)) (kick) (hat) )
  (o/at (m (+ 7 beat-num))        (weak-hat) )
  (o/apply-at (m (+ 8 beat-num)) phat-beats m (+ 8 beat-num) [])
  )

(comment
  (u/••• (phat-beats metro (metro)))
  (o/stop)
  )

;; and combining ideas from sounds.clj with the rhythm ideas here:

;; first we bring back the dubstep inst

(o/definst dubstep [freq 100 wobble-freq 5]
  (let [sweep (o/lin-exp (o/lf-saw wobble-freq) -1 1 40 5000)
        son   (o/mix (o/saw (* freq [0.99 1 1.01])))]
    (o/lpf son sweep)))

;; define a vector of frequencies from a tune
;; later, we use (cycle notes) to repeat the tune indefinitely

(def notes (vec (map (comp o/midi->hz o/note) [:g1 :g2 :d2 :f2 :c2 :c3 :bb1 :bb2
                                               :a1 :a2 :e2 :g2 :d2 :d3 :c2 :c3])))

;; bass is a function which will play the first note in a sequence,
;; then schedule itself to play the rest of the notes on the next beat

(defn bass [m num notes]
  (o/at (m num)
        (o/ctl dubstep :freq (first notes)))
  (o/apply-at (m (inc num)) bass m (inc num) (next notes) []))

;; wobble changes the wobble factor randomly every 4th beat

(defn wobble [m num]
  (o/at (m num)
        (o/ctl dubstep :wobble-freq
               (o/choose [4 6 8 16])))
  (o/apply-at (m (+ 4 num)) wobble m (+ 4 num) []))

;; put it all together

(comment
  (u/•••
   (metro :bpm 180)
   (dubstep) ;; start the synth, so that bass and wobble can change it
   (bass metro (metro) (cycle notes))
   (wobble metro (metro))
   )
  (o/stop)
  )
