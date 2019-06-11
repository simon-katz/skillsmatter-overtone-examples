(ns skillsmatter-overtone-examples.sounds
  (:require [overtone.live :as o]
            [skillsmatter-overtone-examples.bug-fixes]
            [skillsmatter-overtone-examples.utils :as u]) )

;;;; ___________________________________________________________________________

;; define a piano scale based on samples from freesound.org

(def sample-ids
  {:c 148432, :d 148513, :e 148524, :f  148506
   :g 148503, :a 148488, :b 148479, :c+ 148431})

(def samples
  (into {}
        (for [[note id] sample-ids]
          [note (o/sample (o/freesound-path id))])))

(defn piano [note]
  (let [sample (samples note)]
    (sample)))

(defn play-piano-notes [note-names]
  (let [gap  400
        [n & ns] note-names]
    (piano n)
    (when ns
      (Thread/sleep gap) ; can/should you use `o/at`?
      (play-piano-notes ns))))

(comment
  (u/do-and-return-nil (play-piano-notes [:c :d :e :f :g :a :b :c+])))

;;;; ___________________________________________________________________________

;; First, some basic oscillators

(o/definst basic-sine [freq 440]
  (o/sin-osc freq))

(comment
  (u/do-and-return-nil (basic-sine))     ; tuning A
  (o/stop)
  (u/do-and-return-nil (basic-sine 440)) ; same thing
  (o/stop)
  (u/do-and-return-nil (basic-sine 880)) ; doubling freq goes up one octave
  (o/stop)
  (u/do-and-return-nil (basic-sine 220)) ; halving freq goes down one octave
  (o/stop)
  )

(o/definst basic-saw [freq 440]
  (o/saw freq))

(comment
  (u/do-and-return-nil (basic-saw))
  (o/stop)
  (u/do-and-return-nil (basic-saw 880))
  (o/stop)
  (u/do-and-return-nil (basic-saw 220))
  (o/stop)
  )

(o/definst basic-square [freq 440]
  (o/square freq))

(comment
  (u/do-and-return-nil (basic-square))
  (o/stop)
  (u/do-and-return-nil (basic-square 880))
  (o/stop)
  (u/do-and-return-nil (basic-square 220))
  (o/stop)
  )

;; Combining oscillators by mixing signals

;; whole number multiples of a frequence blend well and change the sound
(o/definst multiple-sines [freq 440]
  (+
   (o/sin-osc freq)
   (o/sin-osc (* 2 freq))
   (o/sin-osc (* 3 freq))))

(comment
  (u/do-and-return-nil (multiple-sines 440))
  (o/stop)
  ;;compare with:
  (u/do-and-return-nil (basic-sine 440))
  (o/stop)
  )

;; slightly different, detuned, frequencies clash and give a harsher
;; sound
;; this example also demonstrates passing a vector of values to an
;; oscillator in order to create multiple instances and mix them together
(o/definst detuned-saws [freq 440]
  (o/mix (o/saw (* freq [0.99 1 1.01]))))

(comment
  (u/do-and-return-nil (detuned-saws))
  (o/stop)
  ;; compare:
  (u/do-and-return-nil (basic-saw))
  (o/stop)
  (u/do-and-return-nil (detuned-saws 100)) ;; compare with below
  (o/stop)
  )

;; we can use one oscillator as the input to another. Here we control
;; the frequency of a sine wave with another sine wave to produce a
;; vibrato effect:
(o/definst wobbled-sin [pitch-freq 440 wobble-freq 5 wobble-depth 5]
  (let [wobbler (* wobble-depth (o/sin-osc wobble-freq))
        freq (+ pitch-freq wobbler)]
    (o/sin-osc freq)))

(comment
  (u/do-and-return-nil (wobbled-sin))
  (o/stop)
  ;; you can try it with deeper, slower wobbles:
  (u/do-and-return-nil (wobbled-sin 440 2 50))
  (o/stop)
  ;; if you make the wobble much faster, strange sounds emerge:
  (u/do-and-return-nil (wobbled-sin 440 100 50))
  (o/stop)
  )

;; Combining the previous two ideas, we have the start of a dubstep
;; oscillator:

(o/definst dubstep [freq 100 wobble-freq 2]
  (let [sweep (o/lin-exp (o/lf-saw wobble-freq) -1 1 40 5000)
        son   (o/mix (o/saw (* freq [0.99 1 1.01])))]
    (o/lpf son sweep)))

(comment
  (u/do-and-return-nil (dubstep))
  (o/stop)
  (u/do-and-return-nil (dubstep 150 3))
  (o/stop)
  (u/do-and-return-nil (dubstep 200 6))
  (o/stop)
  (u/do-and-return-nil (dubstep 50 6))
  (o/stop)
  )

;; we can control the oscillator once it has started
(comment
  (u/do-and-return-nil (dubstep))
  (u/do-and-return-nil (o/ctl dubstep :wobble-freq 4))
  (u/do-and-return-nil (o/ctl dubstep :wobble-freq 10))
  (u/do-and-return-nil (o/ctl dubstep :wobble-freq 2))
  (o/stop)
  )

;; See examples.dubstepbass from the overtone distribution for a much
;; more involved dubstep oscillator!




(comment
  ;; Sometimes it's better to work with notes rather than
  ;; frequencies. First we can use MIDI notes, where middle C is defined
  ;; as 60:
  (o/midi->hz 60)
  ;;=> 261.62556...

  ;; an octave is 12 semitones, so we can add 12 to the midi note to get
  ;; the next octave, which doubles the frequency:
  (o/midi->hz 72)
  ;;=> 523.2511...

  )


;; use it in a synth like this:
(comment
  (u/do-and-return-nil (dubstep (o/midi->hz 60)))
  (o/stop)
  )

(comment
  ;; MIDI notes are more abstract than frequencies, but sometimes it's
  ;; nicer to work with note names. We can use the note function:
  (o/note :c4)
  ;;=> 60
  )

(comment
  (u/do-and-return-nil (dubstep (o/midi->hz (o/note :c4))))
  (o/stop)
  )

;; often, rather than doing the conversion inline, we'll translate a
;; list of notes into a list of frequencies which we'll later play:

(def notes (vec (map (comp o/midi->hz o/note) [:c3 :g3 :c3])))
;;=> (130.8127826502993 195.99771799087463 130.8127826502993)

(comment
  (u/do-and-return-nil (dubstep (notes 0)))
  (o/stop)
  (u/do-and-return-nil (dubstep (notes 1)))
  (o/stop)
  )
