/*
 * =====================================================================
 * (c) Copyright 2004  BBN Technologies
 * =====================================================================
 */

package org.cougaar.qos.qrs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.cougaar.qos.ResourceStatus.BadAttributeValueException;
import org.cougaar.qos.ResourceStatus.CrossesThresholdQualifierFactoryPOA;
import org.cougaar.qos.ResourceStatus.DataValueHolder;
import org.cougaar.qos.ResourceStatus.EveryQualifierFactoryPOA;
import org.cougaar.qos.ResourceStatus.ExceedsThresholdQualifierFactoryPOA;
import org.cougaar.qos.ResourceStatus.MinCredibilityQualifierFactoryPOA;
import org.cougaar.qos.ResourceStatus.MinDeltaQualifierFactoryPOA;
import org.cougaar.qos.ResourceStatus.NoSuchAttributeException;
import org.cougaar.qos.ResourceStatus.Qualifier;
import org.cougaar.qos.ResourceStatus.QualifierFactory;
import org.cougaar.qos.ResourceStatus.QualifierHelper;
import org.cougaar.qos.ResourceStatus.QualifierKind;
import org.cougaar.qos.ResourceStatus.QualifierPOA;
import org.cougaar.qos.ResourceStatus.RSSSubscriber;
import org.cougaar.qos.ResourceStatus.ResourceDescriptionParseException;
import org.cougaar.qos.ResourceStatus.ResourceNode;
import org.cougaar.qos.ResourceStatus.ResourceStatusService;
import org.cougaar.qos.ResourceStatus.ResourceStatusServiceOperations;
import org.cougaar.qos.ResourceStatus.ResourceStatusServicePOATie;
import org.cougaar.qos.ResourceStatus.SomeQualifierFactoryPOA;
import org.cougaar.qos.ResourceStatus.data_types;
import org.cougaar.qos.ResourceStatus.data_value;
import org.cougaar.qos.ResourceStatus.data_valueHolder;

import org.omg.CosNaming.NameComponent;
import org.omg.PortableServer.Servant;

public class ResourceStatusServiceImpl implements ResourceStatusServiceOperations {

    private final Map<RSSSubscriber, List<RSSSubscriberProxy>> subscribers;
    private QualifierFactory[] factories;
    private final Logger debugLogger;
    private final Logger eventLogger;
    private final TimerQueueingDataFeed corbaFeed;
    private final Servant servant;

    protected ResourceStatusServiceImpl() {
        subscribers = new HashMap<RSSSubscriber, List<RSSSubscriberProxy>>(); // subscriber -> proxy
        debugLogger = Logging.getLogger(ResourceStatusServiceImpl.class);
        eventLogger = Logging.getEventLogger(ResourceStatusServiceImpl.class);

        corbaFeed = new TimerQueueingDataFeed();
        servant = makeServant();

    }

    public static NameComponent[] DEFAULT_RSS_NAME =
            {new NameComponent("com", ""), new NameComponent("bbn", ""),
                    new NameComponent("ResourceStatus", ""),
                    new NameComponent("ResourceStatusService", ""),};

    protected Servant makeServant() {
        return new ResourceStatusServicePOATie(this);
    }

    protected Servant getServant() {
        return servant;
    }

    protected NameComponent[] getNSName() {
        return DEFAULT_RSS_NAME;
    }

    protected String guiTitle() {
        return "RSS";
    }

    // Make the qualifier factories
    protected void postInit() {
        RSS.instance().registerFeed(corbaFeed, "Corba");

        // This shouldn't be hardwired...
        factories = new QualifierFactory[6];
        try {
            MinDeltaQualifierFactoryPOA fact1 = new MinDeltaQualifierFactoryImpl();
            CorbaUtils.poa.activate_object(fact1);
            factories[QualifierKind.min_delta.value()] = fact1._this();

            MinCredibilityQualifierFactoryPOA fact2 = new MinCredibilityQualifierFactoryImpl();
            CorbaUtils.poa.activate_object(fact2);
            factories[QualifierKind.min_credibility.value()] = fact2._this();

            ExceedsThresholdQualifierFactoryPOA fact3 = new ExceedsThresholdQualifierFactoryImpl();
            CorbaUtils.poa.activate_object(fact3);
            factories[QualifierKind.exceeds_threshold.value()] = fact3._this();

            CrossesThresholdQualifierFactoryPOA fact4 = new CrossesThresholdQualifierFactoryImpl();
            CorbaUtils.poa.activate_object(fact4);
            factories[QualifierKind.crosses_threshold.value()] = fact4._this();

            EveryQualifierFactoryPOA fact5 = new EveryQualifierFactoryImpl();
            CorbaUtils.poa.activate_object(fact5);
            factories[QualifierKind.every.value()] = fact5._this();

            SomeQualifierFactoryPOA fact6 = new SomeQualifierFactoryImpl();
            CorbaUtils.poa.activate_object(fact6);
            factories[QualifierKind.some.value()] = fact6._this();

        } catch (Exception ex) {
            debugLogger.error(ex.toString(), ex);
        }

        if (debugLogger.isDebugEnabled()) {
            debugLogger.debug("postInit()");
        }

        register();
    }

    protected void register() {
        NameComponent[] name = getNSName();
        if (name != null) {
            CorbaUtils.nsBind(name, servant);
        }
    }

    public void pushString(String key, String value) {
        if (eventLogger.isInfoEnabled()) {
            eventLogger.info("Method entry: pushString " + key + "->" + value);
        }
        corbaFeed.newData(key, new DataValue(value), null);
        if (eventLogger.isInfoEnabled()) {
            eventLogger.info("Method exit: pushString " + key + "->" + value);
        }
    }

    public void pushLong(String key, int value) {
        if (eventLogger.isInfoEnabled()) {
            eventLogger.info("Method entry: pushLong " + key + "->" + value);
        }
        corbaFeed.newData(key, new DataValue(value), null);
        if (eventLogger.isInfoEnabled()) {
            eventLogger.info("Method exit: pushLong " + key + "->" + value);
        }
    }

    public boolean invoke_s(String path_string, String method, String[] args)
            throws org.cougaar.qos.ResourceStatus.NoSuchMethodException,
            ResourceDescriptionParseException

    {
        ResourceNode[] path = PathParser.parsePath(path_string);
        return invoke(path, method, args);
    }

    public boolean invoke(ResourceNode[] path, String method, String[] args)
            throws org.cougaar.qos.ResourceStatus.NoSuchMethodException {
        boolean success = false;
        String logmsg = null;
        if (eventLogger.isInfoEnabled()) {
            StringBuffer msg = new StringBuffer();
            msg.append("invoke ");
            msg.append(method);
            msg.append(" path = ");
            RSSUtils.pathToString(msg, path);
            if (args.length > 0) {
                msg.append(" with args ");
                msg.append(args[0]);
                for (int i = 1; i < args.length; i++) {
                    msg.append(", ");
                    msg.append(args[i]);
                }
            }
            logmsg = msg.toString();
            eventLogger.info("Method entry: " + logmsg);
        }
        ResourceContext context = RSS.instance().getPathContext(path);

        if (context != null) {
            try {
                context.invoke(method, args);
                success = true;
            } catch (ResourceContext.NoSuchMethodException ex) {
                throw new org.cougaar.qos.ResourceStatus.NoSuchMethodException(ex.getMethod());
            }
        } else {
            if (eventLogger.isInfoEnabled()) {
                StringBuffer msg = new StringBuffer();
                msg.append("No such context as ");
                RSSUtils.pathToString(msg, path);
                eventLogger.info(msg.toString());
            }
        }

        if (eventLogger.isInfoEnabled()) {
            eventLogger.info("Method exit: " + logmsg + " returning " + success);
        }
        return success;
    }

    public void addDependency(ResourceNode node,
                              ResourceNode[] node_dependencies,
                              ResourceStatusService[] ignore) {
        String logmsg = null;
        if (eventLogger.isInfoEnabled()) {
            StringBuffer msg = new StringBuffer();
            msg.append("addDependency ");
            RSSUtils.nodeToString(msg, node);
            msg.append(":");
            RSSUtils.pathToString(msg, node_dependencies);
            logmsg = msg.toString();
            eventLogger.info("Method entry: " + logmsg);
        }
        // // Set up the RelayFeed
        // DataFeed relay_feed = new PromiscuousRelayDataFeed(rss_dependencies);
        // RSS.instance().registerFeed(relay_feed, "Relay");
        // Create and notify the context
        ResourceContext context = RSS.instance().resolveSpec(node.kind, node.parameters);
        if (context != null) {
            context.setDependencies(node_dependencies);
        }
        if (eventLogger.isInfoEnabled()) {
            eventLogger.info("Method exit: " + logmsg);
        }

    }

    // An Observer which listens for a callback from a
    // BoundDataFormula. If it gets one it fills in a
    // DataValueHolder with the value in the callback and wakes up
    // the waitForCallback thread.
    private class FormulaObserver implements java.util.Observer {
        DataValueHolder result;
        NotificationQualifier qualifier;
        boolean succeeded = false;

        FormulaObserver(DataValueHolder result, NotificationQualifier qualifier) {
            this.result = result;
            this.qualifier = qualifier;
        }

        public void update(java.util.Observable o, Object value) {
            synchronized (this) {
                // fill in 'result' from value
                DataValue v = (DataValue) value;
                if (v != null && qualifier.shouldNotify(v)) {
                    result.value = v.getCorbaValue();
                    succeeded = true;
                    if (eventLogger.isInfoEnabled()) {
                        eventLogger.info("value = " + v);
                    }
                    this.notifyAll();
                }
            }
        }
    }

    private boolean waitForCallback(BoundDataFormula bdf,
                                    long timeout,
                                    NotificationQualifier qualifier,
                                    DataValueHolder result) {
        FormulaObserver observer = new FormulaObserver(result, qualifier);
        bdf.addObserver(observer);
        Runnable creator = bdf.getDelayedFormulaCreator();
        Thread thread = new Thread(creator);
        synchronized (observer) {
            thread.start();
            try {
                observer.wait(timeout);
            } catch (InterruptedException ex) {
            }
        }

        if (!observer.succeeded) {
            eventLogger.warn("blockingQuery timed out");
            result.value = DataValue.NO_VALUE.getCorbaValue();
        }
        bdf.deleteObserver(observer);
        bdf.unsubscribe();
        return observer.succeeded;
    }

    // The two blockingQuery calls should take a qualifier. For now
    // used a canned one that just ensures the value has positive
    // credibility.
    private static final NotificationQualifier Not_No_Value = new NotificationQualifier() {
        public boolean shouldNotify(DataValue value) {
            return value.getCredibility() > 0.0;
        }
    };

    public boolean blockingQuery_s(String path_string, long timeout, DataValueHolder result)
            throws ResourceDescriptionParseException {

        ResourceNode[] path = PathParser.parsePath(path_string);
        return blockingQuery(path, timeout, result);
    }

    public boolean blockingQuery(ResourceNode[] path, long timeout, DataValueHolder result) {
        boolean success = false;
        String logmsg = null;

        if (eventLogger.isInfoEnabled()) {
            StringBuffer buf = new StringBuffer();
            buf.append("blockingQuery path = ");
            RSSUtils.pathToString(buf, path);
            logmsg = buf.toString();
            eventLogger.info("Method entry: " + logmsg);
        }

        try {
            BoundDataFormula bdf = new BoundDataFormula(path, true, null);
            success = waitForCallback(bdf, timeout, Not_No_Value, result);
        } catch (NullFormulaException ex) {
            // Can't be null
            eventLogger.warn("Invalid formula");
            result.value = DataValue.NO_VALUE.getCorbaValue();
            success = false;
        }
        if (eventLogger.isInfoEnabled()) {
            eventLogger.info("Method exit: " + logmsg + " returning " + success);
        }
        return success;
    }

    public boolean query_s(String path_string, DataValueHolder result)
            throws ResourceDescriptionParseException {

        ResourceNode[] path = PathParser.parsePath(path_string);
        return query(path, result);
    }

    public boolean query(ResourceNode[] path, DataValueHolder result) {
        boolean success = false;
        String logmsg = null;

        if (eventLogger.isInfoEnabled()) {
            StringBuffer buf = new StringBuffer();
            buf.append("query path = ");
            RSSUtils.pathToString(buf, path);
            logmsg = buf.toString();
            eventLogger.info("Method entry: " + logmsg);
        }

        DataFormula formula = null;
        formula = RSS.instance().getPathFormula(path);

        if (formula != null) {
            DataValue v = formula.computeValue(true); // ??
            result.value = v.getCorbaValue();
            if (eventLogger.isInfoEnabled()) {
                eventLogger.info("value = " + v);
            }
            success = true;
        } else {
            // Can't be null
            eventLogger.warn("Invalid formula");
            result.value = DataValue.NO_VALUE.getCorbaValue();
            success = false;
        }

        if (eventLogger.isInfoEnabled()) {
            eventLogger.info("Method exit: " + logmsg + " returning " + success);
        }

        return success;
    }

    public QualifierFactory getQualifierFactory(QualifierKind kind) {
        if (eventLogger.isInfoEnabled()) {
            eventLogger.info("getQualifierFactory: " + kind);
        }
        int tag = kind.value();
        return factories[tag];
    }

    public boolean qualifiedSubscribe_s(RSSSubscriber subscriber,
                                        String path_string,
                                        int callback_id,
                                        Qualifier qualifier)
            throws ResourceDescriptionParseException {
        ResourceNode[] path = PathParser.parsePath(path_string);
        return qualifiedSubscribe(subscriber, path, callback_id, qualifier);
    }

    public boolean qualifiedSubscribe(RSSSubscriber subscriber,
                                      ResourceNode[] path,
                                      int callback_id,
                                      Qualifier qualifier) {
        String logmsg = null;
        boolean success = false;
        if (eventLogger.isInfoEnabled()) {
            StringBuffer buf = new StringBuffer();
            if (qualifier == null) {
                buf.append("unqualifiedSubscribe ");
            } else {
                buf.append("qualifiedSubscribe ");
            }
            buf.append(" path = ");
            RSSUtils.pathToString(buf, path);
            if (qualifier != null) {
                buf.append(" qualifier type: ");
                buf.append(qualifier.getClass());
            }
            logmsg = buf.toString();
            eventLogger.info("Method entry: " + logmsg);
        }
        NotificationQualifier nqualifier = null;
        if (qualifier != null) {
            try {
                nqualifier = (NotificationQualifier) CorbaUtils.poa.reference_to_servant(qualifier);
            } catch (Exception ex) {
                eventLogger.error(ex.toString(), ex);
            }
        }
        try {
            BoundDataFormula bdf = new BoundDataFormula(path, true, nqualifier);
            RSSSubscriberProxy proxy = new RSSSubscriberProxy(bdf, subscriber, callback_id, this);
            synchronized (subscribers) {
                List<RSSSubscriberProxy> proxies = subscribers.get(subscriber);
                if (proxies == null) {
                    proxies = new ArrayList<RSSSubscriberProxy>();
                    subscribers.put(subscriber, proxies);
                }
                proxies.add(proxy);
            }
            Runnable r = bdf.getDelayedFormulaCreator();
            RSSUtils.schedule(r, 500);
            success = true;
        } catch (Exception ex) {
            eventLogger.error(ex.toString(), ex);
            success = false;
        }

        if (eventLogger.isInfoEnabled()) {
            eventLogger.info("Method exit: " + logmsg + " returning = " + success);
        }
        return success;

    }

    public boolean unqualifiedSubscribe_s(RSSSubscriber subscriber,
                                          String path_string,
                                          int callback_id) throws ResourceDescriptionParseException {
        ResourceNode[] path = PathParser.parsePath(path_string);
        return qualifiedSubscribe(subscriber, path, callback_id, null);
    }

    public boolean unqualifiedSubscribe(RSSSubscriber subscriber,
                                        ResourceNode[] path,
                                        int callback_id) {
        return qualifiedSubscribe(subscriber, path, callback_id, null);
    }

    public void unsubscribe_s(RSSSubscriber subscriber, String path_string)
            throws ResourceDescriptionParseException {
        ResourceNode[] path = PathParser.parsePath(path_string);
        unsubscribe(subscriber, path);
    }

    public void unsubscribe(RSSSubscriber subscriber, ResourceNode[] path) {
        String logmsg = null;

        if (eventLogger.isInfoEnabled()) {
            StringBuffer buf = new StringBuffer();
            buf.append("unsubscribe path = ");
            RSSUtils.pathToString(buf, path);
            logmsg = buf.toString();
            eventLogger.info("Method entry: " + logmsg);
        }
        synchronized (subscribers) {
            List<RSSSubscriberProxy> proxies = subscribers.get(subscriber);
            if (proxies != null) {
                Iterator<RSSSubscriberProxy> itr = proxies.iterator();
                while (itr.hasNext()) {
                    RSSSubscriberProxy proxy = itr.next();
                    if (proxy.hasPath(path)) {
                        proxy.unbind();
                        itr.remove();
                        break;
                    }
                }
            }
        }

        if (eventLogger.isInfoEnabled()) {
            eventLogger.info("Method exit: " + logmsg);
        }
    }

    // Standalone CORBA-accessible RSS
    public static void main(String[] args) {
        CorbaUtils.main(args, new ResourceStatusServiceImpl());
    }

    // NotificationQualifier classes

    private static class EveryQualifier extends QualifierPOA implements NotificationQualifier {
        Qualifier[] qualifiers;

        EveryQualifier(Qualifier[] qualifiers) {
            this.qualifiers = qualifiers;
        }

        public boolean shouldNotify(DataValue value) {
            for (Qualifier element : qualifiers) {
                NotificationQualifier qualifier = (NotificationQualifier) element;
                if (!qualifier.shouldNotify(value)) {
                    return false;
                }
            }
            return true;
        }

        public void getAttribute(String attr_name, data_valueHolder attr_value)
                throws NoSuchAttributeException {
            throw new NoSuchAttributeException(attr_name);
        }

        public void setAttribute(String attr_name, data_value attr_value)
                throws NoSuchAttributeException {
            throw new NoSuchAttributeException(attr_name);
        }
    }

    private static class SomeQualifier extends QualifierPOA implements NotificationQualifier {
        Qualifier[] qualifiers;

        SomeQualifier(Qualifier[] qualifiers) {
            this.qualifiers = qualifiers;
        }

        public boolean shouldNotify(DataValue value) {
            for (Qualifier element : qualifiers) {
                NotificationQualifier qualifier = (NotificationQualifier) element;
                if (qualifier.shouldNotify(value)) {
                    return true;
                }
            }
            return false;
        }

        public void getAttribute(String attr_name, data_valueHolder attr_value)
                throws NoSuchAttributeException {
            throw new NoSuchAttributeException(attr_name);
        }

        public void setAttribute(String attr_name, data_value attr_value)
                throws NoSuchAttributeException {
            throw new NoSuchAttributeException(attr_name);
        }
    }

    private static abstract class ThresholdQualifierPOA extends QualifierPOA
        implements
            NotificationQualifier {
        double threshold;

        ThresholdQualifierPOA(double threshold) {
            this.threshold = threshold;
        }

        public void getAttribute(String attr_name, data_valueHolder holder)
                throws BadAttributeValueException, NoSuchAttributeException {
            data_value attr_value = holder.value;
            if (!attr_name.equals("threshold")) {
                throw new NoSuchAttributeException(attr_name);
            } else if (attr_value.discriminator() != data_types.number_data) {
                throw new BadAttributeValueException(attr_name);
            } else {
                attr_value.d_value(threshold);
            }
        }

        public void setAttribute(String attr_name, data_value attr_value)
                throws BadAttributeValueException, NoSuchAttributeException {
            if (!attr_name.equals("threshold")) {
                throw new NoSuchAttributeException(attr_name);
            } else if (attr_value.discriminator() != data_types.number_data) {
                throw new BadAttributeValueException(attr_name);
            } else {
                this.threshold = attr_value.d_value();
            }
        }

    }

    private static class MinDeltaQualifier extends ThresholdQualifierPOA {
        DataValue last;

        MinDeltaQualifier(double threshold) {
            super(threshold);
        }

        boolean sufficientChange(DataValue value) {
            double delta = value.getDoubleValue() - last.getDoubleValue();
            return delta < 0 ? -delta > threshold : delta > threshold;
        }

        public boolean shouldNotify(DataValue value) {
            if (last == null || sufficientChange(value)) {
                last = value;
                return true;
            } else {
                return false;
            }
        }

    }

    private static class MinCredibilityQualifier extends ThresholdQualifierPOA {
        MinCredibilityQualifier(double threshold) {
            super(threshold);
        }

        public boolean shouldNotify(DataValue value) {
            return value.getCredibility() > threshold;
        }

    }

    private static class ExceedsThresholdQualifier extends ThresholdQualifierPOA {
        ExceedsThresholdQualifier(double threshold) {
            super(threshold);
        }

        public boolean shouldNotify(DataValue value) {
            return value.getDoubleValue() > threshold;
        }

    }

    private static class CrossesThresholdQualifier extends ThresholdQualifierPOA {
        DataValue last;

        CrossesThresholdQualifier(double threshold) {
            super(threshold);
        }

        boolean sufficientChange(DataValue value) {
            double delta = value.getDoubleValue() - last.getDoubleValue();
            return delta < 0 ? -delta > threshold : delta > threshold;
        }

        public boolean shouldNotify(DataValue value) {
            boolean notify = false;
            if (last == null) {
                // first time
                notify = value.getDoubleValue() > threshold;
            } else if (last.getDoubleValue() <= threshold) {
                // was below
                notify = value.getDoubleValue() > threshold;
            } else {
                // was above
                notify = value.getDoubleValue() <= threshold;
            }
            last = value;
            return notify;
        }

    }

    private static Qualifier getQualifierReference(QualifierPOA impl) {
        org.omg.CORBA.Object reference = null;
        try {
            CorbaUtils.poa.activate_object(impl);
            reference = CorbaUtils.poa.servant_to_reference(impl);
        } catch (Exception ex) {
            Logger logger = Logging.getLogger(ResourceStatusServiceImpl.class);
            logger.error(ex.toString(), ex);
        }
        return QualifierHelper.narrow(reference);
    }

    private static class EveryQualifierFactoryImpl extends EveryQualifierFactoryPOA {
        public Qualifier getQualifier(Qualifier[] qualifiers) {
            QualifierPOA impl = new EveryQualifier(qualifiers);
            return getQualifierReference(impl);
        }
    }

    private static class SomeQualifierFactoryImpl extends SomeQualifierFactoryPOA {
        public Qualifier getQualifier(Qualifier[] qualifiers) {
            QualifierPOA impl = new SomeQualifier(qualifiers);
            return getQualifierReference(impl);
        }
    }

    private static class MinDeltaQualifierFactoryImpl extends MinDeltaQualifierFactoryPOA {
        public Qualifier getQualifier(double threshold) {
            QualifierPOA impl = new MinDeltaQualifier(threshold);
            return getQualifierReference(impl);
        }
    }

    private static class ExceedsThresholdQualifierFactoryImpl
        extends
            ExceedsThresholdQualifierFactoryPOA {
        public Qualifier getQualifier(double threshold) {
            QualifierPOA impl = new ExceedsThresholdQualifier(threshold);
            return getQualifierReference(impl);
        }
    }

    private static class CrossesThresholdQualifierFactoryImpl
        extends
            CrossesThresholdQualifierFactoryPOA {
        public Qualifier getQualifier(double threshold) {
            QualifierPOA impl = new CrossesThresholdQualifier(threshold);
            return getQualifierReference(impl);
        }
    }

    private static class MinCredibilityQualifierFactoryImpl
        extends
            MinCredibilityQualifierFactoryPOA {
        public Qualifier getQualifier(double threshold) {
            QualifierPOA impl = new MinCredibilityQualifier(threshold);
            return getQualifierReference(impl);
        }
    }

}
