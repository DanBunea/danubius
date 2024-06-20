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
            time (-> state :date :time)]

        (d/pre
         (d/props {:class "cl-12 cs-12"})
         (d/text  (contrib.str/pprint-str state)))

        (when (= :list (:_state state))
          (d/div
           (d/div
            (d/a
             (d/on "click" (e/fn [_] (routes/navigate :new-place {})))
             (d/text "Add place")))))

        (when (= :history (:_state state))
          (d/div
           (d/div
            (d/a
             (d/on "click" (e/fn [_] (dispatch [:REMOVE-DATE nil])))
             (d/text "Now")))))

        (d/div
         (d/div
          (e/server
           (e/for-by :db/id [tx (places-tr/find-all-places-transactions)]
                     (let [tx-id (:db/id tx)
                           time (:db/txInstant tx)
                           user-id (:tx/user-id tx)]
                       (e/client
                        (d/div
                         (d/props {:id (str "show_" tx-id)})
                         (d/a
                          (d/on "click" (e/fn [_] (dispatch [:CHANGE-DATE {:db/id tx-id :time time}])))
                          (d/text (str time " - " tx-id))))
                        (d/div (d/text user-id))))))))

        (d/div
         {:id "grid"}

         (e/server
          (e/for-by :db/id [place (places/find-all-places topic time)]
                    (let [id (:db/id place)
                          name (:place/name place)]
                      (e/client
                       (when (= :list (:_state state))
                         (d/div
                          (d/props {:id (str "edit_" id)})
                          (d/a
                           (d/on "click" (e/fn [_] (e/client (routes/navigate :place {:path-params {:id id}}))))
                           (d/text "edit"))))
                       (d/div (d/text name))))))))))))

