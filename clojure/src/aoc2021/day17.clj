(ns aoc2021.day17)

(defn peak [y vy]
  (if (zero? vy) y
    (recur (+ y vy) (dec vy))))

(peak 0 9) ; 45

; part 1: init vy is ymax-1
(peak 0 (- 74 1)) ; 2701

(defn capped-trajectory [x vx]
  (if (zero? vx) [x]
     (cons x (capped-trajectory (+ x vx) (dec vx)))))

(defn valid-vx? [vx x-min x-max]
  (<= x-min (last (take-while #(<= % x-max) (capped-trajectory 0 vx)))))

(defn trajectory-y [y vy]
  (map first (iterate (fn [[a b]] [(+ a b) (dec b)]) [y vy])))

(defn trajectory-x [y vy]
  (map first (iterate (fn [[a b]] [(+ a b) (max 0 (dec b))]) [y vy])))

(defn valid-vy? [vy y-min y-max]
  (>= y-max (last (take-while #(>= % y-min) (trajectory-y 0 vy)))))

(defn hits-box? [vx vy x-min x-max y-min y-max]
  (let [[x y] (last (take-while (fn [[x y]] (and (<= x x-max) (>= y y-min)))
                                (map vector
                                     (trajectory-x 0 vx)
                                     (trajectory-y 0 vy))))]
    (and (>= x x-min)
         (<= y y-max))))

(count (for [vx (filter #(valid-vx? % 20 30) (range 0 (inc 30)))
             vy (filter #(valid-vy? % -10 -5) (range -10 (inc 10)))
             :when (hits-box? vx vy 20 30 -10 -5)]
         [vx vy])) ; 112

(time (count (for [vx (filter #(valid-vx? % 281 311) (range 0 (inc 311)))
             vy (filter #(valid-vy? % -74 -54) (range -74 (inc 74)))
             :when (hits-box? vx vy 281 311 -74 -54)]
         [vx vy]))) ; 1070

