(ns electric-starter-app.ui.places
  (:require
   [hyperfiddle.electric :as e]
   [hyperfiddle.electric-dom2 :as d]
   #?(:cljs [electric-starter-app.routes :as routes])
   #?(:clj [electric-starter-app.signals :refer [signal]])
   ;;   [electric-starter-app.controller :refer [dispatch-to-machine]]
   #?(:clj [electric-starter-app.use-cases.places :as places])))

(e/defn Places [{:keys [user-id]}]
  (e/server
    ;; (let [!at (signal "topic-places")
        ;;   topic (e/watch !at)
        ;;   ]
   (e/client
    (let [!state (atom {:ver 0
                        :_state :list
                        :user {:user/id user-id}})
          state (e/watch !state)
            ;;   dispatch (partial dispatch-to-machine c/entity-list-machine !state)
          ]

      (when (= :list (:_state state))
        (d/div
         (d/div)
         (d/div
          {:id "grid"}

          (e/server
           (e/for-by :db/id [place (places/find-all-places)]
                     (let [id (:db/id place)
                           name (:place/name place)]
                       (e/client
                        (d/div
                         (d/props {:id (str "edit_" id)})
                         (d/a
                          (d/on "click" (e/fn [_] (e/client (routes/navigate :place {:path-params {:id id}}))))
                          (d/text "edit")))

                        (d/div (d/text name)))))))))))
                        ;; )
   ))