(ns electric-starter-app.main
  (:require [hyperfiddle.electric :as e]
            [hyperfiddle.electric-dom2 :as d]
            #?(:cljs [electric-starter-app.routes :as routes])
            [electric-starter-app.ui.places :refer [Places]]
            [electric-starter-app.ui.place-form :refer [Place]]))

;; Saving this file will automatically recompile and update in your browser

(e/defn Main [_ring-request]
  (e/client
   (let [match routes/re-router]
     (binding [d/node js/document.body
               routes/route-match match
               routes/route-name (some-> match :data :name)
               routes/re-router]
       
       (case routes/route-name
         :places
         (Places. {:user-id "mrx"})

         :new-place
         (Place.  {:user-id "mrx"})

         :place
         (Place.  {:id      (some-> match :path-params :id)
                   :user-id "mrx"})

         :history
         (d/h1 (d/text (str "History " (some-> match :path-params :id))))

         ;;default
         (d/div
          (d/h1 (d/text "Hello from Electric Clojure"))
          (d/p (d/text "Source code for this page is in ")
               (d/code (d/text "src/electric_start_app/main.cljc")))
          (d/a
           (d/on "click" (e/fn [_]
                           (e/client
                            (routes/navigate :places {:path-params {}}))))
           (d/text "Places"))))))))
