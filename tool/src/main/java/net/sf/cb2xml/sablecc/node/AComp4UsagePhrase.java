/* This file was generated by SableCC (http://www.sablecc.org/). */
package net.sf.cb2xml.sablecc.node;

import net.sf.cb2xml.sablecc.analysis.*;

import java.util.*;


public final class AComp4UsagePhrase extends PUsagePhrase
{
    private TComp4 _comp4_;
    private TNative _native_;

    public AComp4UsagePhrase ()
    {
    }

    public AComp4UsagePhrase (TComp4 _comp4_, TNative _native_)
    {
        setComp4 (_comp4_);

        setNative (_native_);
    }

    public Object clone ()
    {
        return new AComp4UsagePhrase((TComp4) cloneNode (_comp4_),
            (TNative) cloneNode (_native_));
    }

    public void apply (Switch sw)
    {
        ((Analysis) sw).caseAComp4UsagePhrase (this);
    }

    public TComp4 getComp4 ()
    {
        return _comp4_;
    }

    public void setComp4 (TComp4 node)
    {
        if (_comp4_ != null)
        {
            _comp4_.parent (null);
        }

        if (node != null)
        {
            if (node.parent () != null)
            {
                node.parent ().removeChild (node);
            }

            node.parent (this);
        }

        _comp4_ = node;
    }

    public TNative getNative ()
    {
        return _native_;
    }

    public void setNative (TNative node)
    {
        if (_native_ != null)
        {
            _native_.parent (null);
        }

        if (node != null)
        {
            if (node.parent () != null)
            {
                node.parent ().removeChild (node);
            }

            node.parent (this);
        }

        _native_ = node;
    }

    public String toString ()
    {
        return "" + toString (_comp4_) + toString (_native_);
    }

    void removeChild (Node child)
    {
        if (_comp4_ == child)
        {
            _comp4_ = null;

            return;
        }

        if (_native_ == child)
        {
            _native_ = null;

            return;
        }
    }

    void replaceChild (Node oldChild, Node newChild)
    {
        if (_comp4_ == oldChild)
        {
            setComp4 ((TComp4) newChild);

            return;
        }

        if (_native_ == oldChild)
        {
            setNative ((TNative) newChild);

            return;
        }
    }
}
