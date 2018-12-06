package jforth;

import java.util.ArrayList;

public final class NonPrimitiveWord extends BaseWord
{
  public NonPrimitiveWord(String name)
  {
    super(name, false, false, null);
  }

  public void addWord(ExecuteIF eif)
  {
    words.add(eif);
  }

  public int getNextWordIndex()
  {
    return words.size() + 1;
  }

  public int execute(OStack dStack, OStack vStack)
  {
    int index = 0;
    int size = words.size();
    while (index < size)
    {
      ExecuteIF eif = words.get(index);
      if (eif instanceof BreakLoopControlWord)
        return 1;
      int increment = eif.execute(dStack, vStack);
      if (increment == 0)
        return 0;
      index += increment;
    }
    return 1;
  }

// --Commented out by Inspection START (11/30/2018 1:21 AM):
//  public void setImmediate()
//  {
//    immediate = true;
//  }
// --Commented out by Inspection STOP (11/30/2018 1:21 AM)

// --Commented out by Inspection START (11/30/2018 1:20 AM):
//  public ArrayList<ExecuteIF> getList()
//  {
//    return words;
//  }
// --Commented out by Inspection STOP (11/30/2018 1:20 AM)

  private final ArrayList<ExecuteIF> words = new ArrayList<>();
}
