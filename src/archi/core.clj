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


(defn make-scripts [edges]
  (let [edges-by-feature (group-by (comp :feature #(nth % 2)) edges)]
    [:script {:language "javascript"} "
var originalStyles = {};\n

function highlightNode(id) {\n
    document.getElementById(id).children[1].setAttribute('fill', '#000000');\n
    document.getElementById(id).children[2].setAttribute('fill', '#ffffff');\n
    document.getElementById(id).children[2].setAttribute('stroke', '#ffffff');\n
}\n

function highlightEdge(id) {\n
    document.getElementById(id).children[1].setAttribute('stroke-width', '8');\n
    document.getElementById(id).children[2].setAttribute('stroke-width', '8');\n
    document.getElementById(id).children[2].children[0].children[0].setAttribute('font-size', '24');\n
}\n

function restoreNode(id) {\n
    document.getElementById(id).children[1].setAttribute('fill', originalStyles[id].fill);\n
    document.getElementById(id).children[2].setAttribute('fill', originalStyles[id].stroke);
    document.getElementById(id).children[2].setAttribute('stroke', originalStyles[id].stroke);
}\n

function restoreEdge(id) {\n
    document.getElementById(id).children[1].setAttribute('stroke-width', '1');\n
    document.getElementById(id).children[2].setAttribute('stroke-width', '1');\n
    document.getElementById(id).children[2].children[0].children[0].setAttribute('font-size', '12');\n
}\n

function enterNode(event) {\n
    var node = event.target;\n"
     (map (fn [[n1 n2 m]]
            (str "if (node.id === '" n1 "' || node.id === '" n2 "') {\n
                         highlightNode('" n1 "');\n
                         highlightNode('" n2 "');\n
                         highlightEdge('" (:id m) "');\n
                     }\n"))
          edges)
     "
}

function leaveNode(event) {\n
    var node = event.target;\n"
     (map (fn [[n1 n2 m]]
            (str "if (node.id === '" n1 "' || node.id === '" n2 "') {\n
                         restoreNode('" n1 "');\n
                         restoreNode('" n2 "');\n
                         restoreEdge('" (:id m) "');\n
                     }\n"))
          edges)
     "
}

var node;\n
"
     (map (fn [n]
            (str "node = document.getElementById('" n "');\n
                           node.addEventListener('mouseenter', enterNode);\n
                           node.addEventListener('mouseleave', leaveNode);\n
                           originalStyles['" n "'] = {};
                           originalStyles['" n "'].fill = node.children[1].getAttribute('fill');\n
                           originalStyles['" n "'].stroke = node.children[2].getAttribute('stroke');\n"))
          (sort (distinct (mapcat #(take 2 %) edges))))

     "

function enterEdge(event) {\n
    var edge = event.target;\n"
     (map (fn [[n1 n2 m]]
            (str "if (edge.id === '" (:id m) "') {\n"
                 (string/join "" (map (fn [[n1 n2 m]]
                                        (str "highlightNode('" n1 "');\n
                         highlightNode('" n2 "');\n
                         highlightEdge('" (:id m) "');\n"))
                                      (edges-by-feature (:feature m))))
                 "}\n"))
          edges)
     "
                           }\n

                            function leaveEdge(event) {\n
                                                       var edge = event.target;\n"
     (map (fn [[n1 n2 m]]
            (str "if (edge.id === '" (:id m) "') {\n"
                 (string/join "" (map (fn [[n1 n2 m]]
                                        (str "restoreNode('" n1 "');\n
                         restoreNode('" n2 "');\n
                         restoreEdge('" (:id m) "');\n"))
                                      (edges-by-feature (:feature m))))
                 "}\n"))
          edges)
     "
}\n

var edge;\n
"
     (map (fn [[n1 n2 m]]
            (str "edge = document.getElementById('" (:id m) "');\n
                           edge.addEventListener('mouseenter', enterEdge);\n
                           edge.addEventListener('mouseleave', leaveEdge);\n"))
          edges)
     "
"]))

(defn wrap-html [filename svg styles scripts]
  (html [:html
         [:head
          [:meta {:content "text-html; charset=utf-8" :http-equiv "Content-type"}]
          [:title filename]
          styles]
         svg
         scripts]))

(defn dot->svg [s]
  (let [{:keys [out err]} (sh/sh "dot" "-Tsvg" :in s)]
    (or
     out
     (println err))))

(defn remove-viewbox [svg]
  (string/replace svg #"viewBox=\"[^\"]+\"" ""))

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
                  (dot->svg)
                  (remove-viewbox))
         styles [:style ""]
         scripts (make-scripts edges)
         html (wrap-html filename svg styles scripts)]
    (spit (str filename ".svg") svg)
    (spit (str filename ".html") html)))
