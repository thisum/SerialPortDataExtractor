package sample;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.FileReader;
import java.io.IOException;
import java.util.Iterator;

/**
 * Created by thisum_kankanamge on 4/9/18.
 */
public class ObjTable
{
    private TableView table = new TableView();
    private final ObservableList<RowObj> dataList = FXCollections.observableArrayList();

    public ObjTable()
    {
        setupTable();
        readDataAndFillTable();
    }

    private void setupTable()
    {
        table.setEditable(false);
        TableColumn statusCol = new TableColumn("Status");
        statusCol.setMinWidth(20);
        statusCol.setCellValueFactory(new PropertyValueFactory<RowObj, String>("status"));

        TableColumn objectCol = new TableColumn("Object");
        objectCol.setMinWidth(150);
        objectCol.setCellValueFactory(new PropertyValueFactory<RowObj, String>("object"));

        TableColumn countCol = new TableColumn("Count");
        countCol.setMinWidth(20);
        countCol.setCellValueFactory(new PropertyValueFactory<RowObj, Integer>("count"));

        table.getColumns().addAll(statusCol, objectCol, countCol);
        table.setItems(dataList);
    }

    private void readDataAndFillTable()
    {
        JSONParser jsonParser = new JSONParser();
        try(FileReader fileReader = new FileReader("objects.json"))
        {
            Object o = jsonParser.parse(fileReader);
            JSONObject jsonObject = (JSONObject)o;

            JSONArray jsonArray = (JSONArray)jsonObject.get("objects");
            Iterator iti = jsonArray.iterator();
            while( iti.hasNext() )
            {
                dataList.add(new RowObj(String.valueOf(iti.next())));
            }
        }
        catch( ParseException | IOException e )
        {
            e.printStackTrace();
        }

    }

    public void updateStatus(int i)
    {
        dataList.forEach(r->r.setStatus(""));

        int row = i % dataList.size();
        row = (row == 0) ? dataList.size()-1 : row-1;
        RowObj rowObj = dataList.get(row);
        rowObj.setCount(rowObj.getCount()+1);
        rowObj.setStatus("->");
        table.refresh();
    }

    public TableView getTable()
    {
        return table;
    }

}
