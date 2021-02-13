(ns athens-transit-md.core-test
  (:require [midje.sweet :as ms]
            [athens-transit-md.core]))

; midje tests are run by issuing 'lein midje'
(ms/tabular
 (ms/fact "I should fail in a tabular way"
          (= ?first ?second) => ?expected-result)
 ?first     ?second     ?expected-result
 "alice"    "raven"     true)

(ms/fact "I should fail in a singular way"
         (= :truthy :falsey) => true)
