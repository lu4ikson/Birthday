package com.lu4ikson.birthday;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class PersonFullAdapter extends RecyclerView.Adapter<PersonFullAdapter.ViewHolder> {

    private final OnDeleteClickListener deleteListener;
    private final OnEditClickListener editListener;
    public PersonFullAdapter(OnDeleteClickListener deleteListener, OnEditClickListener editListener) {
        this.deleteListener = deleteListener;
        this.editListener = editListener;
    }
    private List<Person> items = new ArrayList<>();
    public void setItems(List<Person> newItems) {
        this.items = newItems;
        notifyDataSetChanged();
    }
    //метод редактирования записей
    public interface OnEditClickListener {
        void onEditClick(Person person);
    }


    //метод удаления записей
    private boolean deleteModeEnabled = false;

    public void setDeleteMode(boolean enabled) {
        this.deleteModeEnabled = enabled;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_person_full, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Person person = items.get(position);
        holder.textFullInfo.setText(BirthdayUtils.formatFullText(person));
        holder.buttonDelete.setOnClickListener(v -> deleteListener.onDeleteClick(person));
        holder.buttonDelete.setVisibility(deleteModeEnabled ? View.VISIBLE : View.GONE);
        holder.itemView.setOnClickListener(v -> editListener.onEditClick(person));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public interface OnDeleteClickListener {
        void onDeleteClick(Person person);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textFullInfo;
        ImageButton buttonDelete;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            textFullInfo = itemView.findViewById(R.id.textFullInfo);
            buttonDelete = itemView.findViewById(R.id.buttonDelete);
        }
    }
}