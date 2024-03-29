package br.ufal.ic.p2.wepayu.Utils;

public class Utils {

    public static boolean isNumeric(String str) {
        try {
            Double.parseDouble(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static String formatarSalario(String salarioStr) {
        if (isNumeric(salarioStr)) {
            double salario = Double.parseDouble(salarioStr);
            if (salario % 1 == 0) { // se for inteiro
                return String.format("%.0f,00", salario);
            } else { //se for decimal
                return String.format("%.2f", salario).replace(".", ",");
            }
        } else {
            return salarioStr; // Retornar a string original se não for um número
        }
    }
}
