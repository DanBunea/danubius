(ns electric-starter-app.routes
  (:require
   [hyperfiddle.electric :as e]
   [missionary.core :as m]
   [reitit.core :as r]
   #?(:cljs [reitit.frontend.easy :as rfe])))

(e/def route-match)
(e/def route-name)


(def router
  (r/router
    [["/places" :places]
     ["/place/:id" :place]
     ["/history/:id" :history]
     ]))


#?(:cljs (e/def re-router
           (->> (m/observe
                  (fn [!]
                    (rfe/start!
                      router
                      !
                      {:use-fragment false})))
             (m/relieve {})
             new)))

#?(:cljs (defn navigate [to params]
           (rfe/navigate to params)))

(comment
  (r/match-by-path router "/place/7375377453745")
  nil)












