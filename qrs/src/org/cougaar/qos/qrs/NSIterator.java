/*

 * <copyright>
 *  
 *  Copyright 2002-2007 BBNT Solutions, LLC
 *  under sponsorship of the Defense Advanced Research Projects
 *  Agency (DARPA).
 * 
 *  You can redistribute this software and/or modify it under the
 *  terms of the Cougaar Open Source License as published on the
 *  Cougaar Open Source Website (www.cougaar.org).
 * 
 *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 *  "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 *  LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 *  A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 *  OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 *  SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 *  LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 *  DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 *  THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 *  OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *  
 * </copyright>

 */

package org.cougaar.qos.qrs;

import org.apache.log4j.Logger;

import org.omg.CosNaming.Binding;
import org.omg.CosNaming.BindingHolder;
import org.omg.CosNaming.BindingIterator;
import org.omg.CosNaming.BindingIteratorHolder;
import org.omg.CosNaming.BindingListHolder;
import org.omg.CosNaming.BindingType;
import org.omg.CosNaming.NameComponent;
import org.omg.CosNaming.NamingContext;
import org.omg.CosNaming.NamingContextHelper;

import java.util.Iterator;

public class NSIterator implements Iterator<Object> {
    Logger logger;
    NameComponent[] context_name;
    Binding[] list;
    BindingIterator itr;
    BindingHolder binding_holder = new BindingHolder();
    NamingContext context;
    BindingListHolder list_holder = new BindingListHolder();
    BindingIteratorHolder itr_holder = new BindingIteratorHolder();
    int list_index;
    org.omg.CORBA.Object next;
    String current_name, next_name;

    public static class NameServerException extends Exception {
        NameServerException(Exception cause) {
            super(cause);
        }
    }

    public NSIterator(NameComponent[] context_name) throws NameServerException {
        logger = Logging.getLogger(CorbaUtils.class);
        this.context_name = new NameComponent[context_name.length];
        System.arraycopy(context_name, 0, this.context_name, 0, context_name.length);
        try {
            org.omg.CORBA.Object context_ref = CorbaUtils.nsResolve(context_name);
            context = NamingContextHelper.narrow(context_ref);
        } catch (Exception ex) {
            throw new NameServerException(ex);
        }
        initialize();
    }

    public void initialize() {
        context.list(50, list_holder, itr_holder);
        list = list_holder.value;
        itr = itr_holder.value;
        list_index = 0;
        advance();
    }

    private boolean computeNext(Binding binding) {
        boolean success = false;
        if (binding.binding_type.value() == BindingType._nobject) {
            NameComponent[] name = binding.binding_name;
            NameComponent leaf = name[name.length - 1];
            try {
                next = context.resolve(name);
                next_name = leaf.id;
                success = true;
            } catch (Exception ex) {
                logger.error(null, ex);
            }
        }
        return success;
    }

    private void advance() {
        while (true) {
            if (list_index < list.length) {
                if (computeNext(list[list_index++])) {
                    break;
                }
            } else if (itr != null && itr.next_one(binding_holder)) {
                if (computeNext(binding_holder.value)) {
                    break;
                }
            } else {
                if (itr != null) {
                    itr.destroy();
                }
                next = null;
                break;
            }
        }
    }

    public void remove() {
        throw new RuntimeException("remove() is illegal");
    }

    public Object next() {
        if (next != null) {
            Object current = next;
            current_name = next_name;
            advance();
            return current;
        } else {
            return null;
        }
    }

    public String getName() {
        return current_name;
    }

    public boolean hasNext() {
        return next != null;
    }

}
