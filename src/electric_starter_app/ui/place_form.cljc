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
   :on {:CHANGE {:actions change}}
   :states {:form      {:on {}}}})

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

         (d/div
          (d/props {:class "flex flex-row gap-4 items-center"})
          (d/label
           (d/props {:for "name"})
           (d/text "Name: "))
          (d/input
           (d/props {:type "text"
                     :class "pl-4 block w-full rounded-md border-0 py-1.5 text-gray-900 shadow-sm ring-1 ring-inset ring-gray-300 placeholder:text-gray-400 focus:ring-2 focus:ring-inset focus:ring-indigo-600 sm:text-sm sm:leading-6"
                     :id "name"
                     :placeholder "Name"
                     :value (get-in state [:selected :place/name])})
           (d/on "change" (e/fn [e] (let [label (-> e .-target .-value)]
                                      (dispatch [:CHANGE {:place/name label}]))))))

         (try
           (d/button
            (d/props {:class "rounded-md bg-indigo-600 px-3.5 py-2.5 text-sm font-semibold text-white shadow-sm hover:bg-indigo-500 focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-indigo-600"})
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


