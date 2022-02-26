(ns aoc2021.day16)

(defn hex->bin [string] (Integer/toBinaryString (Integer/parseInt string 16)))

(defn parse-header
  [package]
  (let [byt (hex->bin (subs package 0 3))
        typ (Integer/parseInt (subs byt 3 6) 2)]
    (cond-> {:version (Integer/parseInt (subs byt 0 3) 2) :type typ}
       (not= 4 typ) (assoc :compound-type (Integer/parseInt (subs byt 6 7))))))

(parse-header "D2FE28")
(parse-header "EE00D40C823060")
(parse-header "38006F45291200")

(defn parse-literal [stream]
  stream)

(defn parse-package [stream]
  (when (not-empty stream)
    (let [header (parse-header (subs stream 0 3))]
      (case (:type header)
        4 (parse-literal stream)
        ))))

(parse-package "D2FE28")
(parse-package "EE00D40C823060")
(parse-package "38006F45291200")
