(defproject okex "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}

  ;; 使用utf-8编码编译java代码，默认会使用windows系统的默认编码gbk
  :javac-options ["-encoding" "utf-8"]
  :java-source-paths ["src"]

  :dependencies [[org.clojure/clojure "1.10.0"]
                 [com.cemerick/url "0.1.1"] ;; uri处理
                 [slingshot "0.12.2"] ;; try+ catch+
                 [com.taoensso/timbre "4.10.0"] ;; logging
                 [cheshire/cheshire "5.8.1"] ;; json处理
                 [clj-http "3.9.1"] ;; http client
                 [com.rpl/specter "1.1.2"] ;; map数据结构查询
                 [camel-snake-kebab/camel-snake-kebab "0.4.0"] ;; 命名转换
                 [seesaw "1.5.0"] ;; GUI框架
                 ]
  :main ^:skip-aot okex.core
  :aot :all
  :target-path "target/%s"
  :repl-options {:init-ns okex.core})
