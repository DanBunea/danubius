(ns electric-starter-app.hierarchical-state-machine
  (:require
   [spy.core :as spy]
   [hyperfiddle.rcf :refer [tests]]))

(defn- new-state [old-state-path new-state]
  (if (coll? new-state)
    new-state
    (let [state (-> old-state-path
                    drop-last
                    (concat [new-state])
                    vec)]
      (if (= 1 (count state))
        (first state)
        state))))

(defn- state-path [from-state]
  (if (coll? (:_state from-state))
    (:_state from-state)
    [(:_state from-state)]))

(defn- event-on-path [event-name]
  [:on event-name])

(defn- event-states-on-path [from-state event-name]
  (concat
   [:states]
   (->> (state-path from-state)
        (interpose :states)
        vec)
   [:on
    event-name]))

(defn- warn [text]
  (println ::warn text))

(defn- warn-if-nil [text value]
  (when (nil? value) (warn text))
  value)

(defn- has-multiple-options? [v]
  (and (vector? v)
       (every? coll? v)))

(defn- find-start-point [machine from-state event-name event-params]
  (let [on-path (event-on-path event-name)
        states-path (event-states-on-path from-state event-name)
        context (dissoc from-state :_state)

        result (or (get-in machine on-path)
                   (get-in machine states-path))]

    (if (has-multiple-options? result)
      (->> result
           (filter (fn [b] (let [check-cond (:cond b)]
                             (or (nil? check-cond)
                                 (check-cond context event-params)))))
           first
           (warn-if-nil
            (str "No matching condition for the event " event-name ". Event params " (prn-str event-params) ". Machine: " (prn-str machine))))
      (warn-if-nil
       (str "Event " event-name " not found!. Event params " (prn-str event-params) ". Paths: " (prn-str on-path)  " or " (prn-str states-path) " don't exist in machine: " (prn-str machine))
       result))))

(defn transition [machine from-state [event-name event-params]]
  (let [context (dissoc from-state :_state)
        old-state-path (state-path from-state)
        start-point (find-start-point machine from-state event-name event-params)
        action (:actions start-point)

        result (cond
                 (nil? start-point)
                 from-state

                 (and (map? start-point) (:target start-point))
                 {:_state (new-state old-state-path (:target start-point))}

                 (and (map? start-point) (nil? (:target start-point)))
                 {:_state (new-state old-state-path (:_state from-state))}

                 :else
                 {:_state (new-state old-state-path start-point)})]

    (if action
      (merge result (apply action [context event-params]))
      (merge result context))))

(tests
 "transition should transition from a state to another, with no context"
 (def machine {:on {:GE1 :B
                    :GE2 {:target :C}
                    :GE3 {}}
               :states {:A {:on {:E1 :B}}
                        :B {:on {:E2 {:target :C}
                                 :E3 {}}}
                        :C {:states {:C1 {:on {:EC1 :C2}}
                                     :C2 {:on {:EC2 [:B]
                                               :EC3 {}
                                               :EC4 {:target :C3}
                                               :EC5 {:target [:C :C1]}
                                               :EC6 [{:target :C3 :cond (fn [_ _] false)}
                                                     {:target [:B] :cond (fn [_ _] true)}]
                                               :EC7 [{:target :C3 :cond (fn [_ _] true)}
                                                     {:target [:B] :cond (fn [_ _] false)}]
                                               :EC8 [{:target :C3 :cond (fn [_ _] false)}
                                                     {:target [:B]}]}}

                                     :C3 {}}}}})
 (transition
  machine
  {:_state :A}
  [:E1 {}]) := {:_state :B}

 (transition
  machine
  {:_state :B}
  [:E2 {}]) := {:_state :C}

 (transition
  machine
  {:_state :B}
  [:E3 {}]) := {:_state :B}

 (transition
  machine
  {:_state :A}
  [:GE1 {}]) := {:_state :B}

 (transition
  machine
  {:_state :B}
  [:GE2 {}]) := {:_state :C}

 (transition
  machine
  {:_state :B}
  [:GE3 {}]) := {:_state :B}

 (transition
  machine
  {:_state [:C :C1]}
  [:EC1 {}]) := {:_state [:C :C2]}

 (transition
  machine
  {:_state [:C :C2]}
  [:EC2 {}]) := {:_state [:B]}

 (transition
  machine
  {:_state [:C :C2]}
  [:EC3 {}]) := {:_state [:C :C2]}

 (transition
  machine
  {:_state [:C :C2]}
  [:EC4 {}]) := {:_state [:C :C3]}

 (transition
  machine
  {:_state [:C :C2]}
  [:EC5 {}])
 := {:_state [:C :C1]}

 (transition
  machine
  {:_state [:C :C2]}
  [:EC6 {}])
 := {:_state [:B]}

 (transition
  machine
  {:_state [:C :C2]}
  [:EC7 {}])
 := {:_state [:C :C3]}

 (transition
  machine
  {:_state [:C :C2]}
  [:EC8 {}])
 := {:_state [:B]})

(tests
 "transition should transition from a state to another, preserving the context sent"
 (def machine {:on {:GE1 :B
                    :GE2 {:target :C}
                    :GE3 {}}
               :states {:A {:on {:E1 :B}}
                        :B {:on {:E2 {:target :C}
                                 :E3 {}}}
                        :C {:states {:C1 {:on {:EC1 :C2}}
                                     :C2 {:on {:EC2 [:B]
                                               :EC3 {}
                                               :EC4 {:target :C3}
                                               :EC5 {:target [:C :C1]}
                                               :EC6 [{:target :C3 :cond (fn [_ _] false)}
                                                     {:target [:B] :cond (fn [_ _] true)}]
                                               :EC7 [{:target :C3 :cond (fn [_ _] true)}
                                                     {:target [:B] :cond (fn [_ _] false)}]}}
                                     :C3 {}}}}})
 (transition
  machine
  {:_state :A :something "here"}
  [:E1 {}])
 := {:_state :B  :something "here"}

 (transition
  machine
  {:_state :B  :something "here"}
  [:E2 {}])
 := {:_state :C  :something "here"}

 (transition
  machine
  {:_state :B  :something "here"}
  [:E3 {}])
 := {:_state :B  :something "here"}

 (transition
  machine
  {:_state :A  :something "here"}
  [:GE1 {}])
 := {:_state :B  :something "here"}

 (transition
  machine
  {:_state :B  :something "here"}
  [:GE2 {}])
 := {:_state :C  :something "here"}

 (transition
  machine
  {:_state :B  :something "here"}
  [:GE3 {}])
 := {:_state :B  :something "here"}

 (transition
  machine
  {:_state [:C :C1]  :something "here"}
  [:EC1 {}])
 := {:_state [:C :C2]  :something "here"}

 (transition
  machine
  {:_state [:C :C2] :something "here"}
  [:EC2 {}])
 := {:_state [:B] :something "here"}

 (transition
  machine
  {:_state [:C :C2]  :something "here"}
  [:EC3 {}])
 := {:_state [:C :C2]  :something "here"}

 (transition
  machine
  {:_state [:C :C2]  :something "here"}
  [:EC4 {}])
 := {:_state [:C :C3]  :something "here"}

 (transition
  machine
  {:_state [:C :C2] :something "here"}
  [:EC5 {}])
 := {:_state [:C :C1] :something "here"}

 (transition
  machine
  {:_state [:C :C2] :something "here"}
  [:EC6 {}])
 := {:_state [:B] :something "here"}

 (transition
  machine
  {:_state [:C :C2] :something "here"}
  [:EC7 {}])
 := {:_state [:C :C3] :something "here"})

(tests "transition should transition from a state to another running the action"
       (def machine {:on {:GE1 {:actions (fn [ctx _] (assoc ctx :count 3))  :target :C}}
                     :states {:A {:on {:E1 {:actions (fn [ctx _] (assoc ctx :count 2)) :target :B}}}
                              :B {:on {}}
                              :C {:states {:C1 {:on {:EC2 {:actions (fn [ctx _] (assoc ctx :count 1)) :target :C2}
                                                     :EC3 [{:actions (fn [ctx _] (assoc ctx :count 77)) :target :C2 :cond (fn [_ _] false)}
                                                           {:actions (fn [ctx _] (assoc ctx :count 4)) :target [:B] :cond (fn [_ _] true)}]
                                                     :EC4 [{:actions (fn [ctx _] (assoc ctx :count 5)) :target :C2 :cond (fn [_ _] true)}
                                                           {:actions (fn [ctx _] (assoc ctx :count 99)) :target [:B] :cond (fn [_ _] false)}]}}
                                           :C2 {}}}}})
       (transition
        machine
        {:_state :A}
        [:E1 {}])
       {:_state :B :count 2}

       (transition
        machine
        {:_state :B}
        [:GE1 {}])
       := {:_state :C :count 3}
       (transition
        machine
        {:_state [:C :C1]}
        [:EC2 {}])
       := {:_state [:C :C2] :count 1}

       (transition
        machine
        {:_state [:C :C1]}
        [:EC3 {}])
       := {:_state [:B] :count 4}

       (transition
        machine
        {:_state [:C :C1]}
        [:EC4 {}])
       := {:_state [:C :C2] :count 5})

(tests "transition should transition from a state to another running the action adding to the context received through the transition fn"
       (def machine {:on {:GE1 {:actions (fn [ctx _] (assoc ctx :count 3))  :target :C}}
                     :states {:A {:on {:E1 {:actions (fn [ctx _] (assoc ctx :count 2)) :target :B}}}
                              :B {:on {}}
                              :C {:states {:C1 {:on {:EC2 {:actions (fn [ctx _] (assoc ctx :count 1)) :target :C2}
                                                     :EC3 [{:actions (fn [ctx _] (assoc ctx :count 77)) :target :C2 :cond (fn [_ _] false)}
                                                           {:actions (fn [ctx _] (assoc ctx :count 4)) :target [:B] :cond (fn [_ _] true)}]
                                                     :EC4 [{:actions (fn [ctx _] (assoc ctx :count 5)) :target :C2 :cond (fn [_ _] true)}
                                                           {:actions (fn [ctx _] (assoc ctx :count 99)) :target [:B] :cond (fn [_ _] false)}]}}
                                           :C2 {}}}}})
       (transition
        machine
        {:_state :A :initial 1}
        [:E1 {}])
       := {:_state :B :initial 1 :count 2}

       (transition
        machine
        {:_state :B :initial 1}
        [:GE1 {}])
       := {:_state :C :initial 1 :count 3}

       (transition
        machine
        {:_state [:C :C1] :initial 1}
        [:EC2 {}])
       := {:_state [:C :C2] :initial 1 :count 1}

       (transition
        machine
        {:_state [:C :C1] :initial 1}
        [:EC3 {}])
       := {:_state [:B] :initial 1 :count 4}

       (transition
        machine
        {:_state [:C :C1] :initial 1}
        [:EC4 {}])
       := {:_state [:C :C2] :initial 1 :count 5})

(tests
 "transition should pass context and event params to the condition evaluation functions"
 (def invocations (atom []))
 (defn condition-fn [ctx ev]
   (do
     (swap! invocations conj [ctx ev])
     true))

 (transition
  {:states {:A {:on {:EC2 [{:target :B
                            :cond condition-fn}]}}
            :B {}}}
  {:_state :A :something "here"}
  [:EC2 {:event "params"}]) := {:_state :B :something "here"}

 @invocations := [[{:something "here"} {:event "params"}]])

(tests
 "transition fails when there is no matching condition, warns but doen't do anything"
 (def invocations (atom []))
 (with-redefs [warn #(swap! invocations conj %)]
   (transition
    {:states {:A {:on {:EC2 [{:target :B
                              :cond (fn [_ _] false)}
                             {:target :C
                              :cond (fn [_ _] false)}]}}
              :B {}
              :C {}}}
    {:_state :A}
    [:EC2 {}]) := {:_state :A}

   (-> @invocations
       first
       (clojure.string/starts-with? "No matching condition for the event :EC2")) := true

   (reset! invocations [])

   "transition fails when current state invalid , warns but doen't do anything"
   (transition
    {:states {:A {}}}
    {:_state :not-exists}
    [:EC2 {}])
   := {:_state :not-exists}

   (-> @invocations
       first
       (clojure.string/starts-with?
        "Event :EC2 not found!.")) := true

   (reset! invocations [])

   "transition fails when event invalid , warns but doen't do anything"

   (transition
    {:states {:A {}}}
    {:_state :A}
    [:NOT-EXISTING {}]) :=  {:_state :A}

   (-> @invocations
       first
       (clojure.string/starts-with? "Event :NOT-EXISTING not found!."))
   := true))

