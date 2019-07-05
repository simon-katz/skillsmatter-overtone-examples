(ns skillsmatter-overtone-examples.utils)

;;;; ___________________________________________________________________________

(defmacro •• [& body]
  `(do ~@body))

(defmacro ••• [& body]
  `(do ~@body
       nil))
