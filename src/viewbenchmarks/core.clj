(ns viewbenchmarks.core
  (:use net.cgrand.enlive-html
        clojure.contrib.duck-streams)
  (:require [hiccup.core :as hiccup]
            [clj-html.core :as clj-html]
            [clojure.contrib.str-utils2 :as str-utils]))

(defn clj-html-benchmark [num-items]
  (let [text "Some text"]
    (clj-html/html
      [:html
        [:head
          [:title "Literal String"]]
        [:body
          [:div.example text]
          [:ul.times-table
            (for [n (range 1 num-items)]
              [:li n " * 9 = " (* n 9)])]]])))

(defn hiccup-benchmark [num-items]
  (let [text "Some text"]
    (hiccup/html
      [:html
        [:head
          [:title "Literal String"]]
        [:body
          [:div.example text]
          [:ul.times-table
            (for [n (range 1 num-items)]
              [:li n " * 9 = " (* n 9)])]]]))) 

(defn hint-hiccup-benchmark [num-items]
  (let [text "Some text"]
    (hiccup/html
      [:html
        [:head
          [:title "Literal String"]]
        [:body
          [:div.example #^String text]
          [:ul.times-table
            (for [n (range 1 num-items)]
              [:li #^Number n " * 9 = " (* #^Number n 9)])]]])))

(defn str-benchmark [num-items]
  (let [text "Some text"]
    (str "<html><head><title>Literal String</title</head>"
         "<body><div class=\"example\">" text "</div>"
         "<ul class=\"times-table\">"
         (apply str
           (for [n (range 1 num-items)]
             (str "<li>" n " * 9 = " (* n 9) "</li>")))
         "</ul></body></html>")))

(deftemplate test-template
  "viewbenchmarks/template.html"
  [num-items] 
  [:ul.times-table :li] (clone-for [n (range 1 num-items)]
                                   [:li]
                                   (content (str n " * 9 = " (* 9 n)))))


(defn enlive-benchmark [num-items]
  (apply str (test-template num-items)))

(defsnippet test-snippet
  "viewbenchmarks/template.html"
  [:ul.times-table]
  [num-items]
  [:li] (content (str num-items " * 9 = " (* 9 num-items))))

(deftemplate test-template-with-snippet
  "viewbenchmarks/template.html"
  [num-items]
  [:ul.times-table] (content (map test-snippet (range 1 num-items))))

(defn enlive-snippet-benchmark [num-items]
  (apply str (test-template-with-snippet num-items)))

(def *times* 10000)
(def *tests* 3)

(defmacro time*
  "Evaluates expr and prints the time it took.  Returns the elapsed time"
  [expr]
  `(let [start# (. System (nanoTime))
         _# ~expr]
     (/ (double (- (. System (nanoTime)) start#)) 1000000.0)))

(defn run-benchmark [f]
  (doall
      (for [_ (range 0 *tests*)]
        (time* (dotimes [_ *times*] (f))))))

(defn make-view-benchmarks [num-items]
     [{:title "clj-html"
       :description ""
       :test #(clj-html-benchmark num-items)}
      {:title "hiccup"
       :description ""
       :test #(hiccup-benchmark num-items)}
      {:title "hiccup with typehint"
       :description ""
       :test #(hint-hiccup-benchmark num-items)}
      {:title "str"
       :description ""
       :test #( str-benchmark num-items)}
      {:title "enlive"
       :description ""
       :test #(enlive-benchmark num-items)}
      {:title "enlive with snippet"
       :description ""
       :test #(enlive-snippet-benchmark num-items)}])

(defn run-benchmarks [benchmarks]
  (doall 
   (for [{title :title description :description test :test} benchmarks]
     {:title title
      :description description
      :results (run-benchmark test)})))

 (deftemplate test-template-empty
  "viewbenchmarks/template.html"
  [])

(defn enlive-empty-benchmark []
  (apply str (test-template-empty)))

(deftemplate test-template-selector-only
  "viewbenchmarks/template.html"
  []
  [:ul.times-table] nil)

(defn enlive-empty-selector-only-benchmark []
  (apply str (test-template-selector-only)))

(deftemplate test-two-empty-forms
  "viewbenchmarks/template.html"
  []
  [:ul.times-table] nil
  [:ul.times-table] nil)

(defn enlive-two-empty-forms []
  (apply str (test-two-empty-forms)))

(deftemplate test-four-empty-forms
  "viewbenchmarks/template.html"
  []
  [:ul.times-table] nil
  [:ul.times-table] nil
  [:ul.times-table] nil
  [:ul.times-table] nil)

(defn enlive-four-empty-forms []
  (apply str (test-four-empty-forms)))

(deftemplate test-eight-empty-forms
  "viewbenchmarks/template.html"
  []
  [:ul.times-table] nil
  [:ul.times-table] nil
  [:ul.times-table] nil
  [:ul.times-table] nil
  [:ul.times-table] nil
  [:ul.times-table] nil
  [:ul.times-table] nil
  [:ul.times-table] nil)

(defn enlive-eight-empty-forms []
  (apply str (test-eight-empty-forms)))


(def enlive-benchmarks
     [{:title "enlive original"
       :description "the original test part of the view benchmark"
       :test (fn [] ( enlive-benchmark 13))}
      {:title "enlive template only"
       :description "the original test, but no forms"
       :test enlive-empty-benchmark}
      {:title "enlive one form with no transformation"
       :description "The original test, but with only one rule with no transformation"
       :test enlive-empty-selector-only-benchmark}
      {:title "enlive two forms with no transformation"
       :description "Same as 'enlive one forms with no transformations' but with two copies of that form"
       :test enlive-two-empty-forms}
      {:title "enlive four forms with no transformation"
       :description  "Same as 'enlive one forms with no transformations' but with four copies of that form"
       :test enlive-four-empty-forms}
      {:title "enlive eight forms with no transformation"
       :description "Same as 'enlive one forms with no transformations' but with eight copies of that form"
       :test enlive-eight-empty-forms}
      ])

(defn- prepend-title-to-results [results-set]
  (for [{title :title results :results} results-set]
    (conj results title)))

(defn- pivot-matrix [matrix]
  (apply (partial map (comp reverse (partial conj nil))) matrix))

(defn save-to-csv [file-path matrix]
  (spit file-path
        (str-utils/join "\n" (map (partial str-utils/join " , ") matrix))))

(defn run-enlive-transform-benchmarks [range-from range-to range-step]
  (letfn [(transform-benchmark [n]
                               (println "enlive" n "transforms")
                               (run-benchmark #(enlive-benchmark n))
                               ) ]
    (for [item (range range-from range-to range-step)]
      (transform-benchmark item))))