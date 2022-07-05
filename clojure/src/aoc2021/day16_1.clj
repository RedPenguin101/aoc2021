(ns aoc2021.day16-1
  (:require [clojure.test :refer [deftest testing are]]
            [clojure.string :as str]
            [aoc2021.utils :as u]))

(def header 6)

(defn binary->num [xs] (Long/parseLong (apply str xs) 2))

(comment
  "binary in this program is represented as a string"
  (binary->num "01001")
  ;; => 9 
  )

(defn comp-wrap [op] (fn [a b] (if (op a b) 1 0)))
(def  ops [+ * min max nil (comp-wrap >) (comp-wrap <) (comp-wrap =)])
(defn get-operation [binary] (get ops (binary->num (take 3 (drop 3 binary)))))
(defn get-version   [binary] (binary->num (take 3 binary)))

(declare binary->packets)

(defn binary->literal [binary]
  (let [literal     (mapcat rest (u/take-until #(= \0 (first %)) (partition 5 (drop header binary))))]
    {:version       (get-version binary)
     :value         (binary->num literal)
     :packet-length (+ header (* 5/4 (count literal)))}))

(defn binary->operator [binary]
  (let [bit-type?   (= \0 (first (drop header binary)))
        preamble    (+ header 1 (if bit-type? 15 11))
        length      (binary->num (take (if bit-type? 15 11) (drop (inc header) binary)))
        subpackets  (if bit-type?
                      (binary->packets          (take length (drop preamble binary)))
                      (binary->packets   length              (drop preamble binary)))]

    {:operation     (get-operation binary)
     :version       (get-version binary)
     :subpackets    subpackets
     :packet-length (apply + preamble (map :packet-length subpackets))}))

(defn binary->packets
  ([binary] (binary->packets -1 binary))

  ([packet-limit binary]
   (when-not (or (zero? packet-limit) (every? #(= \0 %) binary))
     (let [packet ((if (get-operation binary) binary->operator binary->literal) binary)]
       (into [packet]
             (binary->packets (dec packet-limit) (drop (:packet-length packet) binary)))))))

(defn version-sum [packet]
  (apply + (:version packet) (map version-sum (:subpackets packet))))

(defn eval-packet [packet]
  (or (:value packet)
      (apply (:operation packet) (map eval-packet (:subpackets packet)))))

(defn hexchar->binary [chr] (str/escape (format "%4s" (java.lang.Integer/toBinaryString (java.lang.Character/digit chr 16))) {\space \0}))

(comment
  (def input (mapcat hexchar->binary (slurp "resources/day16input.txt")))
  (time (version-sum (first (binary->packets input))))
  ;; => 917

  (time (eval-packet (first (binary->packets input))))
  ;; => 2536453523344
  )

(deftest tst
  (testing "version sum"
    (are [input expected] (= expected (version-sum (first (binary->packets (mapcat hexchar->binary input)))))
      "8A004A801A8002F478"             16
      "620080001611562C8802118E34"     12
      "C0015000016115A2E0802F182340"   23
      "A0016C880162017C3686B18A3D4780" 31))
  (testing "evaluation"
    (are [input expected] (eval-packet (first (binary->packets (mapcat hexchar->binary input))))
      "C200B40A82"                      3
      "04005AC33890"                   54
      "880086C3E88112"                  7
      "CE00C43D881120"                  9
      "D8005AC2A8F0"                    1
      "F600BC2D8F"                      0
      "9C005AC2F8F0"                    0
      "9C0141080250320F1802104A08"      1)))
