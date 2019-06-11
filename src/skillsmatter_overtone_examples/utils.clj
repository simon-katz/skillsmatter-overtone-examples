(ns skillsmatter-overtone-examples.utils)

;;;; ___________________________________________________________________________

(defmacro do-and-return-nil [& body]
  `(do ~@body
       nil))
