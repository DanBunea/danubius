(ns electric-starter-app.ui.places
  (:require
   contrib.str
   [hyperfiddle.electric :as e]
   [hyperfiddle.electric-dom2 :as d]
   #?(:cljs [electric-starter-app.routes :as routes])
   #?(:clj [electric-starter-app.signals :refer [signal]])
   [electric-starter-app.controller :refer [dispatch-to-machine]]
   #?(:clj [electric-starter-app.use-cases.places :as places])
   #?(:clj [electric-starter-app.use-cases.places-transactions :as places-tr])))

(defn change-date [ctx tx]
  (assoc ctx :date tx))

(defn dissoc-date [ctx _]
  (dissoc ctx :date))

(def machine
  {:id :places
   :on {:CHANGE-DATE {:actions change-date :target :history}}
   :states {:list      {}
            :history   {:on {:REMOVE-DATE {:actions dissoc-date :target :list}}}}})

(e/defn Places [{:keys [user-id]}]
  (e/server
   (let [!at (signal "topic-places")
         topic (e/watch !at)]
     (e/client
      (let [!state (atom {:_state :list
                          :user {:user/id user-id}})
            state (e/watch !state)
            dispatch (partial dispatch-to-machine machine !state)
            time (-> state :date :time)
            formatter (js/Intl.DateTimeFormat. "fr-FR" (clj->js {:day "2-digit" :month "2-digit" :year "numeric" :hour "2-digit" :minute "2-digit"}))]

        (d/pre
         (d/props {:class "cl-12 cs-12"})
         (d/text  (contrib.str/pprint-str state)))

        (when (= :list (:_state state))
          (d/div
           (d/props {:class "my-8"})
           (d/div
            (d/a
             (d/props {:class "rounded-md bg-indigo-600 px-3.5 py-2.5 text-sm font-semibold text-white shadow-sm hover:bg-indigo-500 focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-indigo-600"})
             (d/on "click" (e/fn [_] (routes/navigate :new-place {})))
             (d/text "Add place")))))

        (when (= :history (:_state state))
          (d/div
           (d/div
            (d/a
             (d/props {:class "rounded-md bg-indigo-600 px-3.5 py-2.5 text-sm font-semibold text-white shadow-sm hover:bg-indigo-500 focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-indigo-600"})
             (d/on "click" (e/fn [_] (dispatch [:REMOVE-DATE nil])))
             (d/text "Now")))))

        (d/div
         (d/props {:class "flow-root my-8"})
         (d/ul
          (d/props  {:class "-mb-8" :role "list"})
          (e/server
           (e/for-by :db/id [tx (places-tr/find-all-places-transactions)]
                     (let [tx-id (:db/id tx)
                           time (:db/txInstant tx)
                           user-id (:tx/user-id tx)]
                       (e/client
                        (d/li
                         (d/props {:id (str "show_" tx-id) :class "relative pb-8"})
                         (d/span
                          (d/props {:class "absolute left-4 top-4 -ml-px h-full w-0.5 bg-gray-200"
                                    :aria-hidden "true"}))
                         (d/div
                          (d/props {:class "relative flex space-x-3"})
                          (d/on "click" (e/fn [_] (dispatch [:CHANGE-DATE {:db/id tx-id :time time}])))
                          (d/div
                           (d/span
                            (d/props {:class "flex h-8 w-8 items-center justify-center rounded-full bg-gray-400 ring-8 ring-white "})))
                          (d/div
                           (d/props {:class (str "flex min-w-0 flex-1 justify-between space-x-4 pt-1.5 " (if (= tx-id (-> state :date :db/id)) "font-bold" ""))})
                           (d/text (str (.format formatter time) " - " tx-id)))))))))))

        (d/div
         {:id "grid" :class "flow-root my-8"}

         (e/server
          (e/for-by :db/id [place (places/find-all-places topic time)]
                    (let [id (:db/id place)
                          name (:place/name place)]

                      (e/client
                       (d/div
                        (d/props {:class "flex flex-row gap-4 my-8"})
                        (when (= :list (:_state state))
                          (d/div
                           (d/props {:id (str "edit_" id)})

                           (d/a
                            (d/props {:class "rounded-md bg-indigo-600 px-3.5 py-2.5 text-sm font-semibold text-white shadow-sm hover:bg-indigo-500 focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-indigo-600"})
                            (d/on "click" (e/fn [_] (e/client (routes/navigate :place {:path-params {:id id}}))))
                            (d/text "edit"))))
                        (d/div (d/text name)))))))))))))

