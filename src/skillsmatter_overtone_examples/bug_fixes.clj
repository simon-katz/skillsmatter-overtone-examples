(ns skillsmatter-overtone-examples.bug-fixes
  (:use [clojure.java.io :only [file]]
        [overtone.libs event counters]
        [overtone.sc server info defaults node dyn-vars]
        [overtone.sc.machinery allocator]
        [overtone.sc.machinery.server connection comms native]
        [overtone.sc server info]
        [overtone.helpers audio-file lib file doc]
        [overtone.sc.util :only [id-mapper]]
        [overtone.config.store :refer [config-get]]))

;;;; ___________________________________________________________________________
;;;; Fix bug in `overtone.sc.buffer/assert-less-than-max-buffers`.
;;;; - Use `max-buffers` instead of `config-max-buffers`.

(defn assert-less-than-max-buffers []
  (when (transient-server?)
    (let [config-max-buffers (config-get [:sc-args :max-buffers])
          default-max-buffers 1024
          max-buffers (or config-max-buffers default-max-buffers)]
      (assert (< (get counters* key 0) max-buffers)
              (str "Allocation of buffer exceeded the max-buffers size: "
                   max-buffers "\n."
                   "This can be configured in overtone config under :sc-args {:max-buffers 2^x}.")))))

(intern 'overtone.sc.buffer
        'assert-less-than-max-buffers
        assert-less-than-max-buffers)
