(ns logic.pb
  (:require [clojure.core.logic :as l]))

(defn simple1[a]
  (l/run 1 [q] (l/== a q)))

(defn prefix?[scoll coll]
  (= 1 (count (l/run 1 [q] (l/appendo scoll q coll)))))

;;
;;
;; evaluation aborted : java.lang.StackOverflowError
;; (l/run 1 [q] (l/fresh [a b c] (l/== q [ 6 3]) (l/appendo a q b) (l/appendo b c [ 1 2 3])))
;; but
;; (l/run 1 [q] (l/fresh [a b c] (l/== q [ 6 3]) (l/appendo b c [ 1 2 3]) (l/appendo a q b)))
;; find sublist 
(defn sublist? [x y]
  (= 1 (count (l/run 1 [q] (l/== q x)
                     (l/trace-s)
                     (l/log q)
                           (l/fresh [a b c] 
                                (l/appendo b c y) 
                                (l/appendo a x b))))))

;;
;; (reduce + (seq q)) :  Don't know how to create ISeq from: clojure.core.logic.LVar 
;; How to project lvar to a list ?
;; How to be more smarter : cut when sum > n 
(defn findSum [n coll]
  (l/run 1 [q]
         (l/== n (reduce + (seq q)))
         (l/fresh [ a b c]
                  (l/appendo b c coll) 
                  (l/appendo a q b))))
                     
;;Maigret's Case
(l/defrel presence who where when)
(l/facts presence [["max" "bar" "mercedi"]
                  ["eric" "bar" "mardi"]
                  ["eve" "hipp" "lundi"]])

(l/defrel jealous who whom)
(l/fact jealous "eve" "marie")

(l/defrel thief where when victim)
(l/facts thief [["hipp" "lundi" "marie"]
               ["bar" "mardi" "jean"]
               ["stade" "jeudi" "luc"]])

(l/defrel nomoney who)
(l/fact nomoney "max")
        
(defn suspect []
  (l/run* [q]
          (l/fresh [where when victim]
                   (l/conde [(nomoney q)] [(jealous q victim)])
                   (presence q where when)
                   (thief where when victim))))


(comment

  Prolog version
append([] ,Y,Y).
append([A|D],Y2,[A|R]) :- append(D,Y2,R).

Why [() _ y] instead [() y y]
  (defne appendo [x y z]
    ([() _ y])
    ([[a . d] _ [a . r]] (appendo d y r)))
)  

;; FACTS:
;; 1. There are 5 houses in 5 different colours.
;; 2. In each house lives a person with a different nationality.
;; 3. These 5 owners drink a certain beverage, smoke a certain brand of cigarette and keep a certain pet.
;; 4. No owners have the same pet, brand of cigaratte, or drink.

;; CLUES:
;; 1. The Brit lives in a red house
;; 2. The Swede keeps a dog
;; 3. The Dane drinks tea
;; 4. The green house is on the left of the white house.
;; 5. The green house owner drinks coffee.
;; 6. The person who smokes Pall Mall keeps birds.
;; 7. The owner of the yellow house smokes Dunhill.
;; 8. The man living in the house right in the center drinks milk
;; 9. The Norwegian lives in the first house.
;; 10. The man who smokes Blend lives next to the one who keeps cats
;; 11. The man who keeps horses lives next to the man who smokes Dunhill
;; 12. The owner who smokes Camel drinks beer
;; 13. The German smokes Marlborough.
;; 14. The Norwegian lives next to the blue house
;; 15. The man who smokes Blend has a neighbour who drinks water.

;; The question is, who keeps the fish?

(comment 
  compte est bon in prolog

  ret(X,[X|L],L).
  ret(X,[Y|L],[Y|M]) :- ret(X,L,M).

  compte(S,L,R) :- compte(S,L,[],R).
  compte(0,_,R,R).
  compte(S,L,LC,R) :- ret(X,L,LS), X=<S, T is S-X, compte(T,LS,[X|LC],R).
)
