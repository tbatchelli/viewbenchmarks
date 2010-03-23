(ns viewbenchmarks.core
  (:use net.cgrand.enlive-html)
  (:require [hiccup.core :as hiccup]
            [clj-html.core :as clj-html]))

(defn clj-html-benchmark []
  (let [text "Some text"]
    (clj-html/html
      [:html
        [:head
          [:title "Literal String"]]
        [:body
          [:div.example text]
          [:ul.times-table
            (for [n (range 1 13)]
              [:li n " * 9 = " (* n 9)])]]])))

(defn hiccup-benchmark []
  (let [text "Some text"]
    (hiccup/html
      [:html
        [:head
          [:title "Literal String"]]
        [:body
          [:div.example text]
          [:ul.times-table
            (for [n (range 1 13)]
              [:li n " * 9 = " (* n 9)])]]])))

(defn hint-hiccup-benchmark []
  (let [text "Some text"]
    (hiccup/html
      [:html
        [:head
          [:title "Literal String"]]
        [:body
          [:div.example #^String text]
          [:ul.times-table
            (for [n (range 1 13)]
              [:li #^Number n " * 9 = " (* #^Number n 9)])]]])))

(defn str-benchmark []
  (let [text "Some text"]
    (str "<html><head><title>Literal String</title</head>"
         "<body><div class=\"example\">" text "</div>"
         "<ul class=\"times-table\">"
         (apply str
           (for [n (range 1 13)]
             (str "<li>" n " * 9 = " (* n 9) "</li>")))
         "</ul></body></html>")))

(deftemplate test-template
  "viewbenchmarks/template.html"
  [] 
  [:ul.times-table :li] (clone-for [n (range 1 13)]
                                   #(at % [:li]
                                        (content (str n " * 9 = " (* 9 n))))))


(defn enlive-benchmark []
  (apply str (test-template)))

(defsnippet test-snippet
  "viewbenchmarks/template.html"
  [:ul.times-table]
  [n]
  [:li] (content (str n " * 9 = " (* 9 n))))

(deftemplate test-template-with-snippet
  "viewbenchmarks/template.html"
  []
  [:ul.times-table] (content (map test-snippet (range 1 13))))

(defn enlive-snippet-benchmark []
  (apply str (test-template-with-snippet)))

(def *times* 10000)

(defn run-benchmark [f]
  (dotimes [_ 3]
    (time (dotimes [_ *times*] (f)))))

(defn run-benchmarks []
  (println "clj-html")
  (run-benchmark clj-html-benchmark)
  
  (println "hiccup")
  (run-benchmark hiccup-benchmark)
  
  (println "hiccup (type-hint)")
  (run-benchmark hint-hiccup-benchmark)
  
  (println "str")
  (run-benchmark str-benchmark)
  
  (println "enlive")
  (run-benchmark enlive-benchmark)
  
  (println "enlive with snippet")
  (run-benchmark enlive-snippet-benchmark))

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

(defn run-enlive-benchmarks []
  (println "enlive original")
  (run-benchmark enlive-benchmark)

  (println "enlive template only")
  (run-benchmark enlive-empty-benchmark)

  (println "enlive one rule with no transformation")
  (run-benchmark enlive-empty-selector-only-benchmark)

  (println "enlive two rules with no transformation")
  (run-benchmark enlive-two-empty-forms)

  (println "enlive four rules with no transformation")
  (run-benchmark enlive-four-empty-forms)

  (println "enlive eight rules with no transformation")
  (run-benchmark enlive-eight-empty-forms)
  )