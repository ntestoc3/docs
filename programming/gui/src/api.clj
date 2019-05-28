(ns api
  (:require [clj-http.client :as http]
            [clj-http.cookies :as cookies]
            [cheshire.core :as json]
            [cemerick.url :refer [url url-encode]]
            [buddy.core.mac :as mac]
            [buddy.core.codecs :as codecs]
            [buddy.core.codecs.base64 :as base64]
            [taoensso.timbre :as log]
            [java-time :as time]
            [camel-snake-kebab.core :refer :all])
  (:use [slingshot.slingshot :only [throw+ try+]]
        com.rpl.specter))

(def base-api-host "https://www.okex.com/")
(def cs (cookies/cookie-store))

(defn base64-enc
  "base64编码字节到字符串"
  [bytes]
  (-> (base64/encode bytes)
      codecs/bytes->str))

(defn hmac-sha256-enc->base64
  [data key]
  (-> (mac/hash data {:key key :alg :hmac+sha256})
      base64-enc))

(defn snake-case-keys
  "把map m的key转换为snake_string"
  [m]
  (transform [MAP-KEYS] ->snake_case_string m))

(defn api-request
  "okex api请求
  `args` 为请求参数， "
  ([path] (api-request path nil))
  ([path args]
   (let [args (snake-case-keys args)
         u (-> (url base-api-host path)
               (assoc :query args)
               str)
         header {:cookie-store cs

                 ;; 代理设置
                 :proxy-host "127.0.0.1"
                 :proxy-port 8080

                 :cookie-policy :standard

                 ;; 跳过https证书验证
                 :insecure? true
                 :accept :json}]
     (try+
      (some-> (http/get (str u) header)
              :body
              (json/decode ->kebab-case-keyword))
      (catch (#{400 401 403 404} (get % :status)) {:keys [status body]}
        (log/warn :api-req "return error" status body)
        {:error (json/decode body ->kebab-case-keyword)})
      (catch [:status 500] {:keys [headers]}
        (log/warn :api-req "server error" headers)
        {:error {:code 500
                 :message "remote server error!"}})
      (catch Object _
        (log/error (:throwable &throw-context) "unexpected error")
        (throw+))))))

(defn get-instruments
  "获取币对信息"
  []
  (api-request "/api/spot/v3/instruments"))

(defn get-spot-instrument-book
  "获取币对深度数据"
  ([instrument-id] (get-spot-instrument-book instrument-id nil))
  ([instrument-id opt]
   (-> (format "/api/spot/v3/instruments/%s/book" instrument-id)
       (api-request opt))))
