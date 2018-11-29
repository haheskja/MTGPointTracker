package com.haheskja.mtgpointtracker;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Locale;

public class GameLogAdapter extends RecyclerView.Adapter<GameLogAdapter.MyViewHolder>  {
    private Context context;
    private ArrayList<Game> games;

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class MyViewHolder extends RecyclerView.ViewHolder {
        private TextView gameNum, gameWin, par_1, par_2, par_3, par_4;

        // each data item is just a string in this case
        public MyViewHolder(View v) {
            super(v);
            gameNum = v.findViewById(R.id.gameNum);
            gameWin = v.findViewById(R.id.gameWin);
            par_1 = v.findViewById(R.id.par_1);
            par_2 = v.findViewById(R.id.par_2);
            par_3 = v.findViewById(R.id.par_3);
            par_4 = v.findViewById(R.id.par_4);
        }

        public void setDetails(Context context, Game game) {
            gameNum.setText(game.getGamename());
            gameWin.setText(context.getString(R.string.gamelog_winner, game.getWinner()));
            par_1.setText(context.getString(R.string.gamelog_points, game.getParticipants().get(0), game.getScore().get(0)));
            par_2.setText(context.getString(R.string.gamelog_points, game.getParticipants().get(1), game.getScore().get(1)));
            par_3.setText(context.getString(R.string.gamelog_points, game.getParticipants().get(2), game.getScore().get(2)));
            par_4.setText(context.getString(R.string.gamelog_points, game.getParticipants().get(3), game.getScore().get(3)));

        }

    }

    public GameLogAdapter(Context context, ArrayList<Game> game) {
            this.context = context;
            this.games = game;
        }

    // Create new views (invoked by the layout manager)
    @Override
    public GameLogAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        View v = LayoutInflater.from(context).inflate(R.layout.gamelog_rows,parent, false);
        return new MyViewHolder(v);

    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        Game game = games.get(position);
        holder.setDetails(context, game);

    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return games.size();
    }
}
