// Author: Álvaro Pereira
// Date: 24-02-2025
package com.example.chinagram;

import android.graphics.Rect;
import android.view.View;
import androidx.recyclerview.widget.RecyclerView;

public class GridSpacingItemDecoration extends RecyclerView.ItemDecoration { // Clase para agregar espaciado personalizado entre posts
    private final int spanCount;    // Número de columnas en la cuadrícula
    private final int spacing;      // Espaciado entre ítems en píxeles
    private final boolean includeEdge; // Flag para incluir márgenes en los bordes externos

    public GridSpacingItemDecoration(int spanCount, int spacing, boolean includeEdge) { // Constructor
        this.spanCount = spanCount;    // Ej: 2 para 2 columnas
        this.spacing = spacing;        // Ej: 16 para 16 píxeles de espaciado
        this.includeEdge = includeEdge; // True para márgenes en bordes, False para espaciado solo interno
    }

    // Método sobrescrito para definir los márgenes (offsets) de cada ítem
    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        // Obtengo la posición del ítem en el adaptador
        int position = parent.getChildAdapterPosition(view);
        int column = position % spanCount; // Calculo la columna actual basada en la posición (0 a spanCount-1)

        if (includeEdge) { // True: Si se incluyen márgenes en los bordes
            outRect.left = spacing - column * spacing / spanCount; // Margen izquierdo: Reduzo el espaciado según la columna para distribuirlo uniformemente
            outRect.right = (column + 1) * spacing / spanCount; // Margen derecho: Aumento el espaciado según la columna siguiente

            if (position < spanCount) { // Margen superior solo para la primera fila
                outRect.top = spacing;
            }
            outRect.bottom = spacing; // Margen inferior para todos los ítems
        }
        else {  // False: Si no se incluyen márgenes en los bordes
            outRect.left = column * spacing / spanCount; // Margen izquierdo: Distribuyo el espaciado interno según la columna
            outRect.right = spacing - (column + 1) * spacing / spanCount; // Margen derecho: Reduzco el espaciado para mantenerlo dentro de los ítems

            if (position >= spanCount) { // Margen superior solo para filas posteriores a la primera
                outRect.top = spacing;
            }
            // Sin margen inferior en los bordes (outRect.bottom queda en 0)
        }
    }
}