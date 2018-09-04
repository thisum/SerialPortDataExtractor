package sample;

/**
 * Created by thisum_kankanamge on 4/9/18.
 */
public class RowObj
{
    private String status="";
    private String object="";
    private int count=0;

    public RowObj( String object)
    {
        this.object = object;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    public String getObject()
    {
        return object;
    }

    public void setObject(String object)
    {
        this.object = object;
    }

    public int getCount()
    {
        return count;
    }

    public void setCount(int count)
    {
        this.count = count;
    }
}
