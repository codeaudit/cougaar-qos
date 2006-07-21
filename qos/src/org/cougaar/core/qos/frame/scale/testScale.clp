(defclass relationship org.cougaar.core.qos.frame.scale.Relationship)
(defclass contains org.cougaar.core.qos.frame.scale.Contains extends relationship)
(defclass level3OnLevel2 org.cougaar.core.qos.frame.scale.Level3OnLevel2 extends contains)
(defclass thing org.cougaar.core.qos.frame.scale.Thing)
(defclass level2 org.cougaar.core.qos.frame.scale.Level2 extends thing)
(defclass level6OnLevel5 org.cougaar.core.qos.frame.scale.Level6OnLevel5 extends contains)
(defclass level1 org.cougaar.core.qos.frame.scale.Level1 extends thing)
(defclass level5OnLevel4 org.cougaar.core.qos.frame.scale.Level5OnLevel4 extends contains)
(defclass level5 org.cougaar.core.qos.frame.scale.Level5 extends thing)
(defclass level1OnRoot org.cougaar.core.qos.frame.scale.Level1OnRoot extends contains)
(defclass level2OnLevel1 org.cougaar.core.qos.frame.scale.Level2OnLevel1 extends contains)
(defclass level4 org.cougaar.core.qos.frame.scale.Level4 extends thing)
(defclass level4OnLevel3 org.cougaar.core.qos.frame.scale.Level4OnLevel3 extends contains)
(defclass root org.cougaar.core.qos.frame.scale.Root extends thing)
(defclass level6 org.cougaar.core.qos.frame.scale.Level6 extends thing)
(defclass level3 org.cougaar.core.qos.frame.scale.Level3 extends thing)
(defclass frame-change org.cougaar.core.qos.frame.Frame$Change)
