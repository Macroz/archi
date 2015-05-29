(ns archi.core-test
  (:require [clojure.test :refer :all]
            [archi.core :refer :all]))

(deftest label->id-test
  (is (= "d_ff_cult__d__" (label->id "d1ff1cult-1d# "))))

(deftest node-test
  (with-redefs [gensym (fn [id] (str "gen" id))]
    (is (= {:id "gennode"
            :label "node" :fontsize "18"}
           (node "node")))))

(deftest node->dot-test
  (is (= {:x 1} (node->dot {:x 1}))))

(deftest random-color-seq-test
  (is (= 0 @random-color-seq)))

(deftest random-color-test
  (with-redefs [random-color-seq (atom 0)]
    (is (= "#ff0000" (random-color)))
    (is (= "#00ff00" (random-color)))
    (is (= @random-color-seq 2))))

(deftest defnode-test)

(deftest defnodes-test)

(deftest feature-test
  (with-redefs [gensym (fn [id] (str "gen" id))]
    (= {:id "genfeature" :descriptions [] :paths []}
       (feature []))
    (= {:id "genfeature" :descriptions [42] :paths [:bar :baz]}
       (feature [42] :bar :baz))))

(deftest described-path?-test
  (is (described-path? ["description" :other :elements]) "described path has string as first item")
  (is (not (described-path? [:notstring {:other :values}])))
  (is (not (described-path? [])) "empty path is not described"))

(deftest feature->nodes-test
  (let [plain-path [1 2 3]
        described-path ["description" 4 5 6]]
    (is (= [[1 2 3] [4 5 6]] (feature->nodes {:paths [plain-path described-path]})))))

(deftest feature-desc-test
  (let [feature {:descriptions ["first" "2nd" "3rd"]}]
    (is (= "first" (feature-short-desc feature)))
    (is (= "first, 2nd, 3rd" (feature-full-desc feature)))))

(deftest path-desc-test
  (is (= nil (path-desc [1 2 3])))
  (is (= "desc" (path-desc ["desc" 1 2 3]))))

(deftest make-feature-edge-test
  (let [feature {:id :fid :descriptions ["1st" "2nd"] :color "#color"}
        path ["pd" 1 2]
        n1 {:id :n1}
        n2 {:id :n2}]
    (with-redefs [gensym (fn [id] (str "gen" id))]
      (is (= [:n1 :n2 {:id "genedge"
                       :feature :fid
                       :color "#color"
                       :label "1st: pd"
                       :labeltooltip "1st, 2nd"
                       :tooltip "1st, 2nd"}]
             (make-feature-edge feature path n1 n2))))))

(deftest feature->edges-test
  (with-redefs [make-feature-edge (fn [feature path n1 n2] (vector (first path) n1 n2))
                random-color (fn [] "#color")]
    (is (= [["pd1" 1 2] ["pd1" 2 3] ["pd2" 1 3]]
           (feature->edges {:paths [["pd1" 1 2 3]
                                    ["pd2" 1 3]]})))))
