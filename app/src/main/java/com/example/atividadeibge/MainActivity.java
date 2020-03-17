package com.example.atividadeibge;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {

    private TextView print;
    private EditText year, cod;
    private ProgressBar progressBar;
    private String link = "";
    private String nome_municipio = "";
    private String estado_municipio = "";
    private int valor_mes[][] = new int[2][12];
    private int beneficiados[] = new int[12];
    private String text = "";
    private int t = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        print = findViewById(R.id.print);
        year = findViewById(R.id.year);
        cod = findViewById(R.id.cod);
        progressBar = findViewById(R.id.progressBar);
    }

    public void search(View view) {
        int ano;
        int cod_mun;
        if (!year.getText().toString().isEmpty()) {
            ano = Integer.parseInt(year.getText().toString());
            if (ano < 2013 || ano > 2019) {
                year.setError("Coloque um ano entre 2013 e 2019");
                year.requestFocus();
                return;
            }
        } else {
            year.setError("O campo 'ano' não pode estar vazio");
            year.requestFocus();
            return;
        }

        if (!cod.getText().toString().isEmpty()) {
            cod_mun = Integer.parseInt(cod.getText().toString());
        } else {
            cod.setError("O campo 'Código do Municipio' não pode estar vazio");
            cod.requestFocus();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        print.setVisibility(View.GONE);
        RequestQueue queue = Volley.newRequestQueue(this);

        for (int i = 1; i <= 12; i++) {
            String mes = "";
            if (i < 10) {
                mes = "0" + i;
            } else {
                mes = "" + i;
            }
            link = "http://www.transparencia.gov.br/api-de-dados/bolsa-familia-por-municipio?mesAno=" + ano + "" + mes + "&codigoIbge=" + cod_mun + "&pagina=1";
            JsonArrayRequest objectRequest = new JsonArrayRequest(Request.Method.GET, link, null, new Response.Listener<JSONArray>() {
                @Override
                public void onResponse(JSONArray response) {
                    progressBar.setVisibility(View.GONE);
                    print.setVisibility(View.VISIBLE);
                    //print.setText(response.toString());
                    print.setMovementMethod(new ScrollingMovementMethod());


                    try {
                        t = t + 1;
                        JSONObject obj = (JSONObject) response.get(0);
                        String data = obj.getString("dataReferencia");
                        String[] dataformat = data.split("/");
                        int index = Integer.parseInt(dataformat[1]);
                        index = index - 1;
                        System.out.println("mes do ano: " + index + " + 1 \nT =" + t);
                        if (nome_municipio.isEmpty()) {
                            JSONObject mun = obj.getJSONObject("municipio");
                            nome_municipio = mun.getString("nomeIBGE");
                            JSONObject uf = mun.getJSONObject("uf");
                            estado_municipio = uf.getString("nome");
                        }
                        beneficiados[index] = obj.getInt("quantidadeBeneficiados");
                        valor_mes[0][index] = index;
                        valor_mes[1][index] = obj.getInt("valor");
                        if (t == 12) {
                            format();
                        }


                    } catch (JSONException e) {
                        e.printStackTrace();
                        System.out.println(e);
                    }

                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    progressBar.setVisibility(View.GONE);
                    print.setVisibility(View.VISIBLE);
                    print.setText("Erro ao carregar dados.");
                }
            });
            queue.add(objectRequest);
        }
    }

    private void format() {
        int totalBeneficiados = 0;
        int valorTotal = 0;
        for (int i = 0; i < 12; i++) {
            totalBeneficiados = totalBeneficiados + beneficiados[i];
            valorTotal = valorTotal + valor_mes[1][i];
        }
        text = text + "NOME: " + nome_municipio + "\n";
        text = text + "ESTADO: " + estado_municipio + "\n";
        text = text + "TOTAL PAGO: R$" + valorTotal + "\n";
        text = text + "MÉDIA DE BENFICIADOS: " + (totalBeneficiados / 12) + "\n";
        valor_mes=sort(valor_mes);
        text = text + "VALOR MES "+valor_mes[1][1]+"\n";
        text = text + "MES COM MAIOR VALOR PAGO: " + getMes(valor_mes[0][11]) + "\n";
        text = text + "MES COM MENOR VALOR PAGO: " + getMes(valor_mes[0][0]) + "\n";
        show();
    }

    private void show() {
        print.setText(text);
    }

    private String getMes(int i) {
        String result = "";
        switch (i + 1) {
            case 1:
                result = "JANEIRO";
                break;
            case 2:
                result = "FEVEREIRO";
                break;
            case 3:
                result = "MARÇO";
                break;
            case 4:
                result = "ABRIL";
                break;
            case 5:
                result = "MAIO";
                break;
            case 6:
                result = "JUNHO";
                break;
            case 7:
                result = "JULHO";
                break;
            case 8:
                result = "AGOSTO";
                break;
            case 9:
                result = "SETEMBRO";
                break;
            case 10:
                result = "OUTUBRO";
                break;
            case 11:
                result = "NOVEMBRO";
                break;
            case 12:
                result = "DEZEMBRO";
                break;
        }
        return result;
    }

    private int[][] sort(int[][] val) {
        for (int i = 0; i < 12; i++) {
            int min_val = val[1][i];
            int min_mes = val[0][i];
            int minId = i;
            for (int j = i+1; j < 12; j++) {
                if (val[1][j] < min_val) {
                    min_val = val[1][j];
                    min_mes = val[0][j];
                    minId = j;
                }
            }
            int temp_val = val[1][i];
            int temp_mes = val[0][i];
            val[1][i] = min_val;
            val[0][i] = min_mes;
            val[1][minId] = temp_val;
            val[0][minId] = temp_mes;
        }
        return val;
    }

}
