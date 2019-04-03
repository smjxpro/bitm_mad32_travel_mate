package pro.smjx.travelmate.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import pro.smjx.travelmate.R;
import pro.smjx.travelmate.events.Expense;


public class ExpensesAdapter extends ArrayAdapter<Expense> {
    private Context context;
    private List<Expense> expenseList;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, hh:mm a");
    private TextView nameTv, timeTv, expenseTv;
    private Date date;

    public ExpensesAdapter(@NonNull Context context, List<Expense> expenseList) {
        super(context, R.layout.expense_chart, expenseList);
        this.expenseList = expenseList;
        this.context = context;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        convertView = inflater.inflate(R.layout.expense_chart, parent, false);

        nameTv = convertView.findViewById(R.id.nameTv);
        timeTv = convertView.findViewById(R.id.timeTv);
        expenseTv = convertView.findViewById(R.id.expenseTv);

        nameTv.setText(expenseList.get(position).getCause());
        date = new Date(expenseList.get(position).getTime());
        timeTv.setText(dateFormat.format(date));
        expenseTv.setText(String.valueOf((int) expenseList.get(position).getAmount())+" TK");

        return convertView;
    }
}
