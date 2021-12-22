(ns aoc2021.day17)

(defn y-step [[y vy]] [(+ y vy) (dec vy)])
(defn x-step [[x vx]] [(+ x vx) (max 0 (dec vx))])

(ffirst (drop-while (comp pos? second) (iterate y-step [0 (dec 74)]))) ; 2701

(defn valid-vx? [vx x-min x-max]
  (<= x-min (first (last (take-while #(and (pos? (second %)) (<= (first %) x-max)) (iterate x-step [0 vx]))))))

(defn valid-vy? [vy y-min y-max]
  (>= y-max (last (take-while #(>= % y-min) (map first (iterate y-step [0 vy]))))))

(defn upper-bound [x-max y-min] (fn [[x y]] (and (<= x x-max) (>= y y-min))))
(defn lower-bound [x-min y-max] (fn [[x y]] (and (>= x x-min) (<= y y-max))))

(defn hits-box? [vx vy x-min x-max y-min y-max]
  (->> (map #(vector (first %1) (first %2)) (iterate x-step [0 vx]) (iterate y-step [0 vy]))
       (take-while (upper-bound x-max y-min))
       (last)
       ((lower-bound x-min y-max))))

(count (for [vx (filter #(valid-vx? % 20 30) (range 1 (inc 30)))
             vy (filter #(valid-vy? % -10 -5) (range -10 (inc 10)))
             :when (hits-box? vx vy 20 30 -10 -5)]
         [vx vy])) ; 112

(time (count (for [vx (filter #(valid-vx? % 281 311) (range 1 (inc 311)))
                   vy (filter #(valid-vy? % -74 -54) (range -74 (inc 74)))
                   :when (hits-box? vx vy 281 311 -74 -54)]
               [vx vy]))) ; 1070

