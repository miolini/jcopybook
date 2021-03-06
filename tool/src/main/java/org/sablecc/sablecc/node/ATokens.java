/* This file was generated by SableCC (http://www.sablecc.org/). */

package org.sablecc.sablecc.node;

import java.util.*;
import org.sablecc.sablecc.analysis.*;

public final class ATokens extends PTokens
{
  private final LinkedList _tokenDefs_ = new TypedLinkedList(new TokenDefs_Cast());

  public ATokens()
  {}

  public ATokens(
    List _tokenDefs_)
  {
    {
      this._tokenDefs_.clear();
      this._tokenDefs_.addAll(_tokenDefs_);
    }

  }
  public Object clone()
  {
    return new ATokens(
             cloneList(_tokenDefs_));
  }

  public void apply(Switch sw)
  {
    ((Analysis) sw).caseATokens(this);
  }

  public LinkedList getTokenDefs()
  {
    return _tokenDefs_;
  }

  public void setTokenDefs(List list)
  {
    _tokenDefs_.clear();
    _tokenDefs_.addAll(list);
  }

  public String toString()
  {
    return ""
           + toString(_tokenDefs_);
  }

  void removeChild(Node child)
  {
    if(_tokenDefs_.remove(child))
    {
      return;
    }

  }

  void replaceChild(Node oldChild, Node newChild)
  {
    for(ListIterator i = _tokenDefs_.listIterator(); i.hasNext();)
    {
      if(i.next() == oldChild)
      {
        if(newChild != null)
        {
          i.set(newChild);
          oldChild.parent(null);
          return;
        }

        i.remove();
        oldChild.parent(null);
        return;
      }
    }

  }

  private class TokenDefs_Cast implements Cast
  {
    public Object cast(Object o)
    {
      PTokenDef node = (PTokenDef) o;

      if((node.parent() != null) &&
          (node.parent() != ATokens.this))
      {
        node.parent().removeChild(node);
      }

      if((node.parent() == null) ||
          (node.parent() != ATokens.this))
      {
        node.parent(ATokens.this);
      }

      return node;
    }
  }
}
