(import org.cougaar.core.qos.frame.scale.*)
(defclass relationship Relationship)
(defclass contains Contains extends relationship)
(defclass level3OnLevel2 Level3OnLevel2 extends contains)
(defclass thing Thing)
(defclass level2 Level2 extends thing)
(defclass level6OnLevel5 Level6OnLevel5 extends contains)
(defclass level1 Level1 extends thing)
(defclass level5OnLevel4 Level5OnLevel4 extends contains)
(defclass level5 Level5 extends thing)
(defclass level1OnRoot Level1OnRoot extends contains)
(defclass level2OnLevel1 Level2OnLevel1 extends contains)
(defclass level4 Level4 extends thing)
(defclass level4OnLevel3 Level4OnLevel3 extends contains)
(defclass root Root extends thing)
(defclass level6 Level6 extends thing)
(defclass level3 Level3 extends thing)
(defclass frame-change org.cougaar.core.qos.frame.Frame$Change)