(ns guestbook.handler.guestbook
  (:require [ataraxy.response :as response]
            [clojure.java.jdbc :as jdbc]
            duct.database.sql
            [integrant.core :as ig]
            [guestbook.view.guestbook :as view]
            [ring.util.response :as ring]
            [struct.core :as st]))


;Boundaries
(defprotocol Guestbook
  (fetch-messages [db])
  (save-message [db db-params])
  (query [db sql]))

(extend-protocol Guestbook
  duct.database.sql.Boundary
  (fetch-messages [{db :spec}]
    (jdbc/query db ["SELECT * FROM guestbook"]))

  (save-message [{db :spec} params]
    (jdbc/insert! db :guestbook params))

  (query    [{:keys [spec]} sql]
    (println "sql is... " (str sql))
    (jdbc/query spec sql)))

;Validations
(def message-schema
  [[:name
    st/required
    st/string]
   [:message
    st/required
    st/string
    {:message "message must contain at least 10 characters"
     :validate #(> (count %) 9)}]])

(defn validate-message [params]
  (st/validate params message-schema {:strip true}))

(defmethod ig/prep-key :guestbook.handler.guestbook/home [_ config]
  (merge {:wibble "Hello There"} config))

(defmethod ig/init-key :guestbook.handler.guestbook/query
  [_ {:as opts :keys [db my-params sql] :or {request '_}}]
  (fn [request]
    (let [params (request :route-params)
          my-id (params :id)
          at    (request :ataraxy/result)
          [_ id] (request :ataraxy/result)
          my-sql [(first sql) id]]
      (println "my-id :" (meta my-id))
      (println "route params :" (str params))
      (println "at :" (str at))
      (println "id :" (str id)))))

;Initialise Keys
(defmethod ig/init-key :guestbook.handler.guestbook/home [_ {:keys [db sql] :as foo} ]
  (println foo)
  (fn [{:keys [flash]}]
    (let [data (merge {:messages (fetch-messages db)}
                      (select-keys flash [:name :message :errors]))]
      [::response/ok (view/home data)])))

(defmethod ig/init-key :guestbook.handler.guestbook/save-message [_ {:keys [db my-params]}]
  (fn [{:keys [params]}]
    (let [[errors params] (validate-message params)]

      (if errors
        (-> (ring/redirect "/")
            (assoc :flash (assoc params :errors errors)))
        (do
          (save-message db (assoc params :timestamp (java.util.Date.)))
          (ring/redirect "/"))))))
































