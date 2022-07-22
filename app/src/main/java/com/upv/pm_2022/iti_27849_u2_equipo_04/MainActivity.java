package com.upv.pm_2022.iti_27849_u2_equipo_04;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import android.app.Dialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

/**
 * Features:
 * On double click in canvas;           create a new state and focus on its name, highlight in blue.
 * On double click inside a state;      change state type.
 * On single click inside a state;      change state name,                        highlight in blue.
 * On pressed click and drag;           Create arrow to another state,            highlight in blue.
 */
public class MainActivity extends AppCompatActivity {

    DragAndDropView vista;
    FrameLayout pantalla;
    RelativeLayout botones;
    Dialog dialog_export, dialog_menu;
    DeleteDialog deleteDialog;
    private static final String TAG = "Main_Activity";
    private Bitmap bitmap;
    private Canvas canvas;
    private final static String STR_REGION="\\documentclass[12pt]{article}\n\\usepackage{tikz}\n" +
                                           "\n\\begin{document}\n\n\\begin{center}\n\\begin{tikz" +
                                           "picture}[scale=0.2]\n\\tikzstyle{every node}+=[inner" +
                                           " sep=0pt]\n";
    private final static String END_REGION="\\end{tikzpicture}\n\\end{center}\n\n\\end{document}\n";
    private final static String FILE_NAME    = "fsm_output";
    private final static float RESIZE_FACTOR = (float)0.04;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        deleteDialog = new DeleteDialog(this);

        vista = new DragAndDropView(this);
        pantalla = new FrameLayout(this);
        botones = new RelativeLayout(this);
        dialog_export = new Dialog(MainActivity.this);
        dialog_export.setContentView(R.layout.dialog_export);
        dialog_export.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,
                                            ViewGroup.LayoutParams.WRAP_CONTENT);

        dialog_menu = new Dialog(MainActivity.this);
        dialog_menu.setContentView(R.layout.dialog_menu);
        dialog_menu.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,
                                          ViewGroup.LayoutParams.WRAP_CONTENT);

        //Creación del botón
        Button btnExportar = new Button(this);
        btnExportar.setText("Export");

        //Parametros del botón
        RelativeLayout.LayoutParams lyt = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,RelativeLayout.LayoutParams.WRAP_CONTENT);

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,RelativeLayout.LayoutParams.MATCH_PARENT);

        //Se le agregan los parametos al botón
        botones.setLayoutParams(params);

        // Se agrega el botón
        botones.addView(btnExportar);

        //Se le pone el alineamiento al botón
        lyt.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
        lyt.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
        lyt.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE);
        btnExportar.setLayoutParams(lyt);


        //clic listener al botón
        btnExportar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //System.out.println("Funciona");
                dialog_export.show();
            }
        });

        Button expLatex = dialog_export.findViewById(R.id.btnExpLatex);
        Button expPNG = dialog_export.findViewById(R.id.btnExpPNG);

        // export canvas to latex
        expLatex.setOnClickListener(view -> {
            File file = new File(Environment.getExternalStorageDirectory().toString() +
                                 '/' + FILE_NAME + ".tex");
            try{
                file.delete(); file.createNewFile();
                FileWriter out = new FileWriter(Environment.getExternalStorageDirectory()
                                                .toString() + '/' + FILE_NAME + ".tex");
                out.append(toLatex(vista.getAllFigures())); out.flush(); out.close();
                Toast.makeText(getBaseContext(), "Tex file exported into root folder",
                               Toast.LENGTH_LONG).show();
            } catch (Exception e) {
                Toast.makeText(getBaseContext(), "An error occurred", Toast.LENGTH_LONG).show();
                e.printStackTrace();
            }
        });

        //Listener para PNG
        expPNG.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View v) {

                capturaPantalla(getWindow().getDecorView());
                vista.bitmapToImage();
                bitmap = save(vista);

                File file = new File(Environment.getExternalStorageDirectory().toString() +
                        '/' + FILE_NAME + ".png");
                try {
                    file.delete(); file.createNewFile();
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, new FileOutputStream(file));
                    Toast.makeText(getBaseContext(), "PNG file exported into root folder",
                            Toast.LENGTH_LONG).show();
                } catch (Exception e) {
                    Toast.makeText(getBaseContext(), "An error occurred", Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                }


            }
        });
        // want fullscreen, we hide Activity's title and notification bar
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                             WindowManager.LayoutParams.FLAG_FULLSCREEN);

        //Se agregan tanto el surface view como los botones a una misma pantalla
        pantalla.addView(vista);
        pantalla.addView(botones);

        //Se muestra
        setContentView(pantalla);

        //setContentView(new DragAndDropView(this));
    }


    private File capturaPantalla(View v) {
        View rootview = v.getRootView();
        rootview.setDrawingCacheEnabled(true);
        Bitmap bmp = rootview.getDrawingCache();
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.JPEG, 40, bytes);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd-HHmmss", Locale.getDefault());
        String fechaComoCadena = sdf.format(new Date());

        File file = new File(Environment.getExternalStorageDirectory() + File.separator + fechaComoCadena + ".jpg");
        try {
            if (file.createNewFile()) {
                try (FileOutputStream outputStream = new FileOutputStream(file)) {
                    outputStream.write(bytes.toByteArray());
                    outputStream.close();
                }
            }
        }catch (Exception e) {
            e.printStackTrace();
        }
        rootview.setDrawingCacheEnabled(false);
        return file;
    }



    @RequiresApi(api = Build.VERSION_CODES.O)
    Bitmap save(View v) {
        Bitmap b = Bitmap.createBitmap(v.getWidth(), v.getHeight(), Bitmap.Config.RGBA_F16);
        Canvas c = new Canvas(b);
        v.draw(c);
        return b;
    }


    /**
     * Get the latex representation of the graph
     * @param figures a list containing nodes and arrows
     * @return string with latex representation of the diagram
     */
    private static String toLatex(ArrayList<Figure> figures) {
        String latex_output = STR_REGION;
        for(Figure figure : figures)
            latex_output += figure.toLatex(RESIZE_FACTOR);
        return latex_output + END_REGION;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        //TODO: Cambiar switch por un if ya que solo se tiene un caso
        switch(item.getItemId()){
            case R.id.acerca_de:
                //Equipo
                dialog_menu.show();

                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}