(ns archi.core
  (:require [clojure.string :as string])
  (:require [clojure.java.shell :as sh])
  (:require [clojure.java.browse :as browse])
  (:require [tangle.core :as tangle])
  (:use [hiccup.core :only [html]]))

(defn label->id [label]
  (.replaceAll label "[^a-zA-Z]" "_"))

(defn node [label]
  {:id (gensym (label->id label)) :label label :fontsize "18"})

(defn node->dot [n] n)

(def random-color-seq (atom 0))

(defn random-color []
  (let [i @random-color-seq]
    (swap! random-color-seq (comp #(mod % 15) inc))
    (case i
      0 "#ff0000"
      1 "#00ff00"
      2 "#0000ff"
      3 "00ffff"
      4 "#ff00ff"
      5 "#cccc00"
      6 "#ff8888"
      7 "#88ff88"
      8 "#8888ff"
      9 "#8800ff"
      10 "#0088ff"
      11 "#ff0088"
      12 "#ff8800"
      13 "#00ff88"
      14 "#88ff00")))

(defmacro defnode [id]
  `(def ~id (node ~(str id))))

(defmacro defnodes [& ids]
  `(do ~@(map (fn [id] (list 'archi.core/defnode id)) ids)))

(defn feature [descriptions & paths]
  {:id (gensym "feature") :descriptions descriptions :paths paths})

(defn described-path? [path]
  (string? (first path)))

(defn feature->nodes [feature]
  (for [path (:paths feature)]
    (let [described? (described-path? path)
          path-desc (when described? (first path))
          path (if described? (rest path) path)]
      path)))

(defn feature-short-desc [feature]
  (first (:descriptions feature)))

(defn feature-full-desc [feature]
  (string/join ", " (:descriptions feature)))

(defn path-desc [path]
  (when (described-path? path) (first path)))

(defn make-feature-edge [feature path n1 n2]
  (let [color (:color feature)
        short-desc (feature-short-desc feature)
        full-desc (feature-full-desc feature)
        path-desc (path-desc path)
        label (str short-desc (when path-desc (str ": " path-desc)))]
    [(:id n1) (:id n2) {:id (gensym "edge")
                        :feature (:id feature)
                        :color color
                        :label label
                        :labeltooltip full-desc
                        :tooltip full-desc}]))

(defn feature->edges [feature]
  (let [color (random-color)
        feature (assoc feature :color color)]
    (apply concat (for [path (:paths feature)]
                    (let [without-desc-path (if (described-path? path) (rest path) path)]
                      (map (partial make-feature-edge feature path)
                           without-desc-path
                           (rest without-desc-path)))))))

(defn wrap-in-quotes [x]
  (str "'" x "'"))

(defn make-scripts [edges]
  (let [edges-by-feature (group-by (comp :feature #(nth % 2)) edges)]
    (println edges)
    (println edges-by-feature)
    [:script {:language "javascript"}
     (str "\n"
          (slurp "src/archi/highlight.js")
          "var data = {};\n"
          (apply str (map (fn [[n1 n2 m]]
                            (str "data['" n1 "'] = data['" n2 "'] = {nodes: ['" n1 "', '" n2 "'], edges: ['" (:id m) "']};\n"))
                          edges))
          (apply str (map (fn [edges]
                            (let [nodes (distinct (sort (mapcat #(take 2 %) edges)))
                                  edges (distinct (sort (map (fn [[n1 n2 m]] (:id m)) edges)))]
                              (str "var nodes = ["
                                   (apply str (interpose ", " (map wrap-in-quotes nodes)))
                                   "];\n"
                                   "var edges = ["
                                   (apply str (interpose ", " (map wrap-in-quotes edges)))
                                   "];\n"
                                   "var d = {nodes: nodes, edges: edges};\n"
                                   (apply str (for [e edges]
                                                (str "data['" e "'] = d;\n"))))))
                          (vals edges-by-feature)))
          "highlight(data);\n"
          )]))

(defn wrap-html [filename svg styles scripts]
  (html [:html
         [:head
          [:meta {:content "text-html; charset=utf-8" :http-equiv "Content-type"}]
          [:title filename]
          styles]
         svg
         scripts]))

(defn render! [features & opts]
  (let [{:keys [node->descriptor edge->descriptor filename]
         :or {node->descriptor (fn [node] node) edge->descriptor (fn [i1 i2 m] m) filename "archi"}} opts
         nodes (map node->dot (apply concat (mapcat feature->nodes features)))
         edges (mapcat feature->edges features)
         options {:directed? true
                  :graph {:rankdir :LR}
                  :node {:shape :box}
                  :node->id :id
                  :node->descriptor node->descriptor
                  :edge->descriptor edge->descriptor}
         svg (->> (tangle/graph->dot nodes edges options)
                  (tangle/dot->svg))
         styles [:style ""]
         scripts (make-scripts edges)
         html (wrap-html filename svg styles scripts)]
    (spit (str filename ".svg") svg)
    (spit (str filename ".html") html)))
