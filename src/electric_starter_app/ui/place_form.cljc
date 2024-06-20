(ns electric-starter-app.ui.place-form
  (:import [hyperfiddle.electric Pending])
  (:require
   contrib.str
   [hyperfiddle.electric :as e]
   [hyperfiddle.electric-dom2 :as d]
   #?(:clj [electric-starter-app.signals :refer [signal]])
   #?(:cljs [electric-starter-app.routes :as routes])
   [electric-starter-app.controller :refer [dispatch-to-machine]]
   #?(:clj [electric-starter-app.use-cases.place :as place])
   #?(:clj [electric-starter-app.use-cases.save :as save])))

(defn change [ctx changes]
  (update ctx :selected merge changes))

(def machine
  {:id :place-form
   :states {:form      {:on {:CHANGE {:actions change}}}}})

(e/defn Place [{:keys [id user-id]}]
  (e/server
   (let [db-id (when id (Long/parseLong id))
         item  (if id (place/find-place db-id) {})]
     (e/client
      (let [!state (atom {:_state :form
                          :selected item
                          :user {:user/id user-id}})
            state (e/watch !state)
            dispatch (partial dispatch-to-machine machine !state)
            on-close #(routes/navigate :places {:path-params {}})]
        (d/div

         (d/pre
          (d/props {:class "cl-12 cs-12"})
          (d/text  (contrib.str/pprint-str state)))

         (when (:error state)
           (d/pre
            (d/props {:class "error"})
            (d/text
             (contrib.str/pprint-str (:error state)))))

         (d/label
          (d/props {:for "name"})
          (d/text "Name: "))
         (d/input
          (d/props {:type "text"
                    :id "name"
                    :placeholder "Name"
                    :value (get-in state [:selected :place/name])})
          (d/on "change" (e/fn [e] (let [label (-> e .-target .-value)]
                                     (dispatch [:CHANGE {:place/name label}])))))

         (try
           (d/button
            (d/on "click" (e/fn [_]
                            (let [transaction [(:selected state)]]
                              (e/server
                               (let [result (save/transact
                                             transaction
                                             user-id)
                                     error (:error result)]
                                 (e/client
                                  (if error
                                    (dispatch [:ERROR {:error error}])
                                                ;;else
                                    (on-close))))))))
            (d/text "Save"))
           (catch Pending _e
             (d/style {:background-color "#f0f0f0"})))))))))


