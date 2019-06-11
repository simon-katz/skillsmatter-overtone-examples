(ns dev)

(defn load-ns-s []
  ;; If I put these in the `ns` form, it hangs when I start a REPL.
  (require '[skillsmatter-overtone-examples.rhythms]
           '[skillsmatter-overtone-examples.sounds]))
