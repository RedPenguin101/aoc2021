(ns aoc2021.chart
  (:require [clojure2d.core :as c2d]))

(defn- draw-axis-lines [canvas h-size v-size]
  (c2d/with-canvas-> canvas
    (c2d/set-color 0 0 0)
    (c2d/set-stroke 2.0)
    (c2d/line (int (* 0.1 h-size)) (int (* 0.9 v-size))
              (int (* 0.9 h-size)) (int (* 0.9 v-size)))
    (c2d/line (int (* 0.1 h-size)) (int (* 0.1 v-size))
              (int (* 0.1 h-size)) (int (* 0.9 v-size)))))

(defn- points->pix [points h-size v-size]
  (let [[xs ys] (apply map vector points)
        x-start (* 0.1 h-size) x-end (* 0.9 h-size)
        x-min (apply min xs) x-max (apply max xs)
        x-factor (/ (- x-end x-start) (- x-max x-min))

        y-start (* 0.1 v-size) y-end (* 0.9 v-size)
        y-min (apply min ys)  y-max (apply max ys)
        y-factor (/ (- y-end y-start) (- y-max y-min))]
    (map vector
         (map #(int (+ x-start (* x-factor (- % x-min)))) xs)
         (map #(int (- v-size (+ y-start (* y-factor (- % y-min))))) ys))))

(defn- draw-points [canvas points h-size v-size]
  (c2d/with-canvas [c canvas]
    (c2d/set-color c :red)
    (doseq [[x y] (points->pix points h-size v-size)]
      (c2d/rect c x y 15 15))))

(defn scatter [points x-pix y-pix]
  (let [canvas (c2d/canvas x-pix y-pix)]
    (draw-axis-lines canvas x-pix y-pix)
    (draw-points canvas points x-pix y-pix)
    (c2d/show-window canvas "Graph")))

(comment
  (draw-scatter [[1 10] [2 15] [3 20]] 1000 1000))