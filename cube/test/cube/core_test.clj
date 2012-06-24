(ns cube.core-test
  (:use clojure.test
        cube.core))

(deftest test-dataset
  (testing "Test Dataset Generation"
    (let [d (generate-dataset 10)]
      (is (= 10 (count d)))
      ))
  (testing "Query Execution"
    (let [d (generate-dataset 5)
          e (execute {:aggregate :region} d)]
      (is (< 0 (count (:rows e)))))))

