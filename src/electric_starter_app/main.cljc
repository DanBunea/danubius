(ns electric-starter-app.main
  (:require [hyperfiddle.electric :as e]
            [hyperfiddle.electric-dom2 :as dom]
            #?(:cljs [electric-starter-app.routes :as routes])))

;; Saving this file will automatically recompile and update in your browser

(e/defn Main [ring-request]
  (e/client
    (let [match routes/re-router
          _ (prn 33 match)]
      (binding [dom/node js/document.body
                routes/route-match match
                routes/route-name (some-> match :data :name)
                routes/re-router
                ]
        (dom/h1 (dom/text "all"))
        (case routes/route-name
            :places
            (dom/h1 (dom/text "Places"))

            :place
            (dom/h1 (dom/text (str "Place " (some-> match :path-params :id))))

            :history
            (dom/h1 (dom/text (str "History " (some-> match :path-params :id))))

         ;;default
            (dom/div
              (dom/h1 (dom/text "Hello from Electric Clojure"))
              (dom/p (dom/text "Source code for this page is in ")
                (dom/code (dom/text "src/electric_start_app/main.cljc"))))))))
                )
