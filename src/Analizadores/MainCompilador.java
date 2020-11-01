package Analizadores;

import java.io.IOException;
import java.util.ArrayList;

public class MainCompilador {

    public static ArrayList<Token> tokens;
    public static ArrayList<Simbolos> tablaSimbolos = new ArrayList<>();

    public static void main(String[] args) throws IOException {
        LeerArchivos arch = new LeerArchivos("test");
        Lexico analizadorLexico = new Lexico();
        tokens = new ArrayList<>();
        String linea = "";
        int l = 0;

        while (linea != null) {
            l++;
            linea = arch.leerSigLinea();
            analizadorLexico.analizarLinea(linea, l, 0);

        }
        recorrerTokens();
        new Sintactico(obtenerTipos(), tokens, tablaSimbolos);

        TablaSimbolos t = new TablaSimbolos(tablaSimbolos);

    }

    private static ArrayList<String> obtenerTipos() {
        ArrayList<String> al = new ArrayList<>();
        for (Token token : tokens)
            al.add(token.getTipo());
        return al;
    }

    public static void agregarToken(String palabra, boolean pres, String tipo, int l, int c) {
        tokens.add(new Token(palabra, pres, tipo, l, c));
    }

    public static void recorrerTokens() {
        Simbolos s;
        boolean compatible;
        for (int i = 0; i < tokens.size(); i++) {
            if (tokens.get(i).getTipo().equals("identifier")) {
                // primero vamos a ver si ya existe previamente la variable en la tabla

                int nTablaSimbolos = encontrarVariable(tokens.get(i).getToken());
                // y si esa variable no se encuentra repetida con un type de dato
                if (nTablaSimbolos == 0) {
                    if (tokens.get(i - 1).getTipo().equals("type")) {
                        if (tokens.get(i + 2).getTipo().equals("boolean literal")
                                || tokens.get(i + 2).getTipo().equals("integer literal")
                                || tokens.get(i + 1).getTipo().equals(";")) {
                            if (tokens.get(i + 1).getTipo().equals(";")) {
                                s = new Simbolos(tokens.get(i).getToken(), tokens.get(i - 1).getToken(), "Empty",
                                        tokens.get(i - 1).getLinea());
                                tablaSimbolos.add(s);
                            } else {
                                compatible = verficiarDeclaracion(tokens.get(i).getToken(),
                                        tokens.get(i - 1).getToken(), tokens.get(i + 2).getToken(),
                                        tokens.get(i - 1).getLinea());
                                if (compatible) {
                                    s = new Simbolos(tokens.get(i).getToken(), tokens.get(i - 1).getToken(),
                                            tokens.get(i + 2).getToken(), tokens.get(i - 1).getLinea());
                                    tablaSimbolos.add(s);
                                }
                            }
                        }
                    }
                } else {
                    // si la variable ya existe en la tablaDeSimbolos

                    if (tokens.get(i - 1).getTipo().equals("type")) {
                        System.out.println("La variable " + tokens.get(i).getToken() + " en la linea "
                                + tokens.get(i).getLinea() + " se encunentra repetida");
                    } else {

                        cuadruplos(getLineOpearacion(i + 1), nTablaSimbolos,
                                tablaSimbolos.get(nTablaSimbolos).getNombre());
                    }

                }
            }
        }
    }

    public static int encontrarVariable(String nombre) {
        int exists = 0;
        for (int i = 0; i < tablaSimbolos.size(); i++) {
            if (tablaSimbolos.get(i).getNombre().equals(nombre)) {
                exists = i;
                break;
            }

        }

        return exists;
    }

    public static boolean variableRepetida(String nombre, int posicion) {
        boolean exists = false;
        System.out.println(nombre);
        for (int i = 0; i < tablaSimbolos.size(); i++) {
            if (tablaSimbolos.get(i).getNombre().equals(nombre)) {
                System.out.println(tablaSimbolos.get(i).getNombre());
                if ("Type".equals(tokens.get(i - 1).getTipo())) {
                    System.out.println(tokens.get(i - 1).getTipo());
                    exists = true;
                    System.out.println(
                            "Variable " + nombre + " en la linea " + posicion + " no se encuentra inicializada");
                    System.exit(0);
                }
            }

        }
        return exists;
    }

    public static void newValue(int i, int iSimmbol) {
        int n1, n2;
        for (int j = i + 1; j < tokens.size(); j++) {
            if (tokens.get(j).getTipo().equals("=")) {
                if (tokens.get(j + 2).getTipo().equals("aritmetical operator")) {
                    String operador = tokens.get(j + 2).getToken();
                    String newValue = "";
                    if (tokens.get(j + 1).getTipo().equals("identifier")) {
                        isInitialize(tokens.get(j + 1).getToken(), tokens.get(j + 1).getLinea());
                        n1 = Integer.parseInt(getValorVariable(tokens.get(j + 1).getToken()));

                    } else {
                        n1 = Integer.parseInt(tokens.get(j + 1).getToken());
                    }
                    if (tokens.get(j + 3).getTipo().equals("identifier")) {
                        isInitialize(tokens.get(j + 3).getToken(), tokens.get(j + 3).getLinea());
                        n2 = Integer.parseInt(getValorVariable(tokens.get(j + 3).getToken()));
                    } else {
                        n2 = Integer.parseInt(tokens.get(j + 3).getToken());
                    }

                    switch (operador) {
                        case "+":
                            newValue = String.valueOf(n1 + n2);
                            tablaSimbolos.get(iSimmbol).setValor(newValue);
                            break;
                        case "-":
                            newValue = String.valueOf(n1 - n2);
                            tablaSimbolos.get(iSimmbol).setValor(newValue);
                            break;
                        case "/":
                            newValue = String.valueOf(n1 / n2);
                            tablaSimbolos.get(iSimmbol).setValor(newValue);
                            break;
                        case "*":
                            newValue = String.valueOf(n1 * n2);
                            tablaSimbolos.get(iSimmbol).setValor(newValue);
                            break;
                        case "%":
                            newValue = String.valueOf(n1 % n2);
                            tablaSimbolos.get(iSimmbol).setValor(newValue);
                            break;
                    }
                }
            }
        }
    }

    public static String getValorVariable(String nombre) {
        String valor = "";
        for (int i = 0; i < tablaSimbolos.size(); i++) {
            if (tablaSimbolos.get(i).getNombre().equals(nombre)) {
                valor = tablaSimbolos.get(i).getValor();
            }
        }
        return valor;
    }

    public static boolean verficiarDeclaracion(String nombre, String tipo, String valor, int linea) {
        boolean compatible = true;
        if (tipo.equals("int") && !isNumeric(valor)) {
            System.out.println(nombre + " es de tipo " + tipo + " y no se le puedo asignar el valor " + valor);
            compatible = false;
            System.out.println(compatible);
        }

        if (tipo.equals("boolean")) {
            if (!valor.equals("false") && !valor.equals("true")) {
                System.out.println(nombre + " es de tipo " + tipo + " y no se le puedo asignar el valor " + valor);
                compatible = false;
            }
        }

        return compatible;
    }

    public static void isInitialize(String nombre, int linea) {
        boolean inicializada = true;
        for (int i = 0; i < tablaSimbolos.size(); i++) {
            if (tablaSimbolos.get(i).getNombre().equals(nombre)) {
                if (tablaSimbolos.get(i).getValor().equals("Empty")) {
                    inicializada = false;
                    System.out.println(
                            "La variable " + nombre + " en la linea " + linea + " no se encuentra inicializada");
                    System.exit(0);
                }
            }
        }

    }

    public static boolean isNumeric(String str) {
        try {
            Double.parseDouble(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static void cuadruplos(String linea, int posicionTablaSim, String nombreVariable) {

        char[] caracteres = linea.toCharArray();
        // los pasamos a un arraylist para manejarlo con mayor facilidad
        ArrayList<Operandos> variables = new ArrayList<>();
        Operandos o;
        for (char c : caracteres) {
            o = new Operandos(String.valueOf(c), String.valueOf(c));
            variables.add(o);
        }
        int contadorTemporales = 0;
        int arturoTemporal = 0;
        // recorrer el array para encontrar los operadores con mayor prioridad
        System.out.println("Operador" + "\t" + "Operando1" + "\t" + "Operando2" + "\t" + "Resultado" + "\t");
        // recorre la expresion para realizar las multiplicaciones y y divisiones
        // primero
        for (int i = 0; i < variables.size(); i++) {
            if (variables.get(i).getNombre().equals("*") || variables.get(i).getNombre().equals("/")) {
                contadorTemporales++;
                System.out.println(variables.get(i).getNombre() + "\t\t" + variables.get(i - 1).getNombre() + "\t\t"
                        + variables.get(i + 1).getNombre() + "\t\t" + "Arturo" + contadorTemporales);

                if (variables.get(i).getNombre().equals("*")) {
                    arturoTemporal = Integer.parseInt(variables.get(i - 1).getValor())
                            * Integer.parseInt(variables.get(i + 1).getValor());
                } else {
                    arturoTemporal = Integer.parseInt(variables.get(i - 1).getValor())
                            / Integer.parseInt(variables.get(i + 1).getValor());
                }
                String temporal = Integer.toString(arturoTemporal);

                // cambiamos el valor en esa posicion por el resultado de la operacion, y
                // eliminamos el simbolo y el otro operador
                variables.get(i + 1).setValor(temporal);
                variables.get(i + 1).setNombre("Arturo" + contadorTemporales);
                variables.remove(i);
                variables.remove(i - 1);
                i = -1;// para que regrese al inicio del array por que si no terminaria el ciclo
                       // despues de haber eliminado las "variables" anteriores
            }
        }

        // recorrido para sumas y restas
        for (int i = 0; i < variables.size(); i++) {
            if (variables.get(i).getNombre().equals("+") || variables.get(i).getNombre().equals("-")) {
                contadorTemporales++;
                if (variables.size() == 3) {// es 3 por que cuando hace la ultima operacion siempre son 3 elementos,
                                            // ambos operandos y el operador
                    System.out.println(variables.get(i).getNombre() + "\t\t" + variables.get(i - 1).getNombre() + "\t\t"
                            + variables.get(i + 1).getNombre() + "\t\t" + nombreVariable);
                } else
                    System.out.println(variables.get(i).getNombre() + "\t\t" + variables.get(i - 1).getNombre() + "\t\t"
                            + variables.get(i + 1).getNombre() + "\t\t" + "Arturo" + contadorTemporales);

                if (variables.get(i).getNombre().equals("+")) {
                    arturoTemporal = Integer.parseInt(variables.get(i - 1).getValor())
                            + Integer.parseInt(variables.get(i + 1).getValor());
                } else {
                    arturoTemporal = Integer.parseInt(variables.get(i - 1).getValor())
                            - Integer.parseInt(variables.get(i + 1).getValor());
                }
                String temporal = Integer.toString(arturoTemporal);

                // cambiamos el valor en esa posicion por el resultado de la operacion, y
                // eliminamos el simbolo y el otro operador
                variables.get(i + 1).setValor(temporal);
                variables.get(i + 1).setNombre("Arturo" + contadorTemporales);
                variables.remove(i);
                variables.remove(i - 1);
                i = -1;// para que regrese al inicio del array por que si no terminaria el ciclo
                       // despues de haber eliminado las "variables" anteriores
            }

        }
        tablaSimbolos.get(posicionTablaSim).setValor(Integer.toString(arturoTemporal));
        System.out.println("Operacion = " + arturoTemporal);
    }

    public static String getLineOpearacion(int posicionTokenIgual) {// usaremos el igual como
                                                                    // punto de
        // partida hasta llegar al
        // ; para obtener la cadena de la expresion
        String expresion = "";
        int i = posicionTokenIgual + 1;// mas 1 para no incluir el igual
        while (!tokens.get(i).getTipo().equals(";")) {

            expresion += tokens.get(i).getToken();
            i++;
        }

        return expresion;
    }

}
