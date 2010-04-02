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

(deftemplate test-template-2
  "viewbenchmarks/template-2.html"
  [iters] 
  [:ul.times-table :li] (clone-for [n (range 1 iters)]
                                   #(at % [:li]
                                        (content (str n " * 9 = " (* 9 n))))))


(defn enlive-benchmark-2 [iters]
  (apply str (test-template-2 iters)))

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

(defn run-benchmark [f]
  (dotimes [_ 3]
    (time (dotimes [_ 100] (f)))))

;(println "clj-html")
;(run-benchmark clj-html-benchmark)

;(println "hiccup")
;(run-benchmark hiccup-benchmark)

;(println "hiccup (type-hint)")
;(run-benchmark hint-hiccup-benchmark)

;(println "str")
;(run-benchmark str-benchmark)

;(println "enlive")
;(run-benchmark enlive-benchmark)

;(println "enlive with snippet")
;(run-benchmark enlive-snippet-benchmark)

(doseq [iters (range 0 1000 100)]
  (println "enlive template-2" iters "iterations")
  (run-benchmark #(enlive-benchmark-2 iters)))