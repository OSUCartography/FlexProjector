/*
 * MatrixUtils.java
 *
 * Created on April 5, 2005, 9:17 PM
 */

package ika.utils;

// import Jama.Matrix;

/**
 * MatrixUtils<br>
 * Utility class for matrix computation<br>
 * <b>Important: This class has not been tested yet!</b>
 * @author Bernhard Jenny<br>
 * Intitute of Cartography<br>
 * ETH Zurich<br>
 */
public class MatrixUtils {
    
    /**
     * Multiply a matrix with another matrix that only contains elements on its
     * diagonal (0 elswhere).
     * @param mat The matrix (m x n).
     * @param diag The matrix that only contains elements on its diagonal, as a one-dimensional
     * vector of size n x 1 that represents the matrix of size n x n.
     * @return The resulting matrix of size m x n.
     */
    public static double [][] matrix_x_diagonalMatrix
            (double[][] mat, double[] diag) {
        final int m = mat.length;
        final int n = mat[0].length;
        
        double[][] res = new double [m][n];
        
        for (int row = 0; row < m; row++) {
            for (int col = 0; col < n; col++){
                res[row][col] = mat[row][col] * diag[col];
            }
        }
        
        return res;
    }
    
    /**
     * First transposes a matrix, then multiplies it with another matrix that
     * only contains elements on its diagonal (0 elswhere).
     * @param mat The matrix (m x n).
     * @param diag The matrix that only contains elements on its diagonal, as a one-dimensional
     * vector of size n x 1 that represents the matrix of size n x n.
     * @return The resulting matrix of size m x n.
     */
    public static double [][] matrixTrans_x_diagonalMatrix
            (double[][] mat, double[] diag) {
        
        final int m = mat.length;
        final int n = mat[0].length;
        
        double[][] res = new double [n][m];
        
        for (int row = 0; row < n; row++) {
            for (int col = 0; col < m; col++){
                res[row][col] = mat[col][row] * diag[col];
            }
        }
        
        return res;
    }
    
    /**
     * Multiplies two matrices.
     * @param A Matrix (m x n).
     * @param B Matrix (n x o).
     * @return The resulting matrix of size m x o.
     */
    public static double[][] matrix_x_matrix(double[][] A, double[][] B) {
        
        final int nA = A[0].length;
        final int mC = A.length;
        final int nC = B[0].length;
        
        final double[][] C = new double[mC][nC];
        
        for (int i = 0; i < mC; i++) {
            final double[] C_row = C[i];
            final double[] A_row = A[i];
            for (int j = 0; j < nC; j++) {
                for (int k = 0; k < nA; k++) {
                    C_row[j] += A_row[k] * B[k][j];
                }
            }
        }
        return C;
    }
    
    /**
     * Multiplies a matrix with another matrix that only  has one column.
     * @param mat The matrix (m x n).
     * @param vector The matrix that only contains one column. It is a
     * two-dimensional array, the parameter vectorCol determines which column
     * holds the vector.
     * @param vectorCol The column that holds the vector data.
     * @return The resulting matrix of size m x 1.
     */
    public static double[] matrix_x_vectorMatrix
            (double[][] mat, double[][] vector, int vectorCol) {
        
        final int m = mat.length;
        final int n = mat[0].length;
        
        double[] res = new double [m];
        
        for (int row = 0; row < m; row++) {
            for (int col = 0; col < n; col++){
                res[col] += mat[row][col] * vector[col][vectorCol];
            }
        }
        
        return res;
    }
    
    /**
     * First transposes a matrix, then multiplies it with another matrix that
     * only contains elements on its diagonal (0 elswhere), then multiplies it
     * again with the matrix.
     * @param mat The matrix (m x n).
     * @param diag The matrix that only contains elements on its diagonal,
     * as a one-dimensional
     * vector of size n x 1 that represents the matrix of size n x n.
     * @return The resulting matrix of size n x n.
     */
    public static double [][] matrixTrans_x_diagonalMatrix_x_Matrix
            (double[][] mat, double[] diag) {
        final int m = mat.length;
        final int n = mat[0].length;
        
        // result is a symetrical square matrix
        double[][] res = new double [n][n];
        
        // row[] will hold intermediate results
        double[] row = new double [m];
        
        for (int r = 0; r < n; r++) {
            // compute row r of ATP
            for (int col = 0; col < m; col++)
                row[col] = mat[col][r] * diag[col];
            
            // multiply row r of ATP with the columns of A
            for (int j = r; j < n; j++) {
                for (int i = 0; i < m; i++)
                    res[r][j] += row[i] * mat[i][j];
                
                // mirror result on diagonal axis
                res[j][r] = res[r][j];
            }
        }
        return res;
    }
    
    /**
     * First transposes a matrix, then multiplies it with the original matrix.
     * @param mat The matrix (m x n).
     * @return The resulting matrix of size n x n.
     */
    public static double[][] matrixTrans_x_matrix(double[][] mat) {
        final int m = mat.length;
        final int n = mat[0].length;
        
        // result is a symetrical square matrix
        double[][] res = new double[n][n];
        
        for (int r = 0; r < n; r++){
            for (int c = r; c < n; c++){
                for (int i = 0; i < m; i++) {
                    res[r][c] += mat[i][r] * mat[i][c];
                }
                // mirror result on diagonal axis
                res[c][r] = res[r][c];
            }
        }
        return res;
    }
    
    /**
     * First transposes matrix matA, then multiplies it with matrix matB.
     * @param matA The matrix (m x n).
     * @param matB The matrix (m x r).
     * @return The resulting matrix of size n x r.
     */
    public static double[][] matrixATrans_x_matrixB(double[][] matA, double[][] matB) {
        final int m = matA[0].length;
        final int n = matB[0].length;
        
        double[][] res = new double[m][n];
        
        for (int r = 0; r < m; r++){
            for (int c = 0; c < n; c++){
                for (int i = 0; i < matA.length; i++) {
                    res[r][c] += matA[i][r] * matB[i][c];
                }
            }
        }
        return res;
    }
    
    /**
     * First transposes matrix A, then multiplies it with diagonal matrix P, then with A.
     * @param A The matrix A (m x n).
     * @param P The matrix P (m x m), must be diagonal.
     * @return The resulting matrix of size n x n.
     */
    public final static double[][] ATPA(double[][] A, double[] P) {
        return matrixTrans_x_diagonalMatrix_x_Matrix(A, P);
    }
    
    /**
     * First transposes matrix A, then multiplies it with diagonal matrix P.
     * @param A The matrix A (m x n).
     * @param P The matrix P (m x m), must be diagonal.
     * @return The resulting matrix of size n x n.
     */
    public final static double[][] ATP(double[][] A, double[] P) {
        return matrixTrans_x_diagonalMatrix(A, P);
    }
    
    /**
     * Inverts a 4 x 4 matrix using Gaussian Elimination.
     * C code from
     * http://www.gamedev.net/community/forums/topic.asp?topic_id=211424&PageSize=25&WhichPage=1
     * ported to java. Not very elegant but it works.
     * @param A The 4 x 4 matrix to invert.
     * @return A new inverted matrix (4 x 4).
     */
    public final static double[][] invertMatrix4x4(double[][] A) {
        
        double s1, s2, s3, s4;
        double negX, div;
        double r1fab, r2fab, r3fab, r4fab;
        
        double[] A_row0 = A[0];
        double[] A_row1 = A[1];
        double[] A_row2 = A[2];
        double[] A_row3 = A[3];
        
        double[][] res = new double[4][4];
        double[] res_row0 = res[0];
        double[] res_row1 = res[1];
        double[] res_row2 = res[2];
        double[] res_row3 = res[3];
        
        for (int i = 0; i < 4; i++)
            res[i][i] = 1;
        
        /*========================= Step 1 of Gaussian Elimination ==============================*/
        
        r1fab = Math.abs(A_row0[0]);
        r2fab = Math.abs(A_row1[0]);
        r3fab = Math.abs(A_row2[0]);
        r4fab = Math.abs(A_row3[0]);
        
        // Pivot Point Test on Column 1
        if( r2fab > r1fab ) {
            s1 = A_row0[0];
            s2 = A_row0[1];
            s3 = A_row0[2];
            s4 = A_row0[3];
            A_row0[0] = A_row1[0];
            A_row0[1] = A_row1[1];
            A_row0[2] = A_row1[2];
            A_row0[3] = A_row1[3];
            A_row1[0] = s1;
            A_row1[1] = s2;
            A_row1[2] = s3;
            A_row1[3] = s4;
            
            s1 = res_row0[0];
            s2 = res_row0[1];
            s3 = res_row0[2];
            s4 = res_row0[3];
            res_row0[0] = res_row1[0];
            res_row0[1] = res_row1[1];
            res_row0[2] = res_row1[2];
            res_row0[3] = res_row1[3];
            res_row1[0] = s1;
            res_row1[1] = s2;
            res_row1[2] = s3;
            res_row1[3] = s4;
        }
        
        if( r3fab > r1fab ) {
            s1 = A_row0[0];
            s2 = A_row0[1];
            s3 = A_row0[2];
            s4 = A_row0[3];
            A_row0[0] = A_row2[0];
            A_row0[1] = A_row2[1];
            A_row0[2] = A_row2[2];
            A_row0[3] = A_row2[3];
            A_row2[0] = s1;
            A_row2[1] = s2;
            A_row2[2] = s3;
            A_row2[3] = s4;
            
            s1 = res_row0[0];
            s2 = res_row0[1];
            s3 = res_row0[2];
            s4 = res_row0[3];
            res_row0[0] = res_row2[0];
            res_row0[1] = res_row2[1];
            res_row0[2] = res_row2[2];
            res_row0[3] = res_row2[3];
            res_row2[0] = s1;
            res_row2[1] = s2;
            res_row2[2] = s3;
            res_row2[3] = s4;
            
        }
        if( r4fab > r1fab ) {
            s1 = A_row0[0];
            s2 = A_row0[1];
            s3 = A_row0[2];
            s4 = A_row0[3];
            A_row0[0] = A_row3[0];
            A_row0[1] = A_row3[1];
            A_row0[2] = A_row3[2];
            A_row0[3] = A_row3[3];
            A_row3[0] = s1;
            A_row3[1] = s2;
            A_row3[2] = s3;
            A_row3[3] = s4;
            
            s1 = res_row0[0];
            s2 = res_row0[1];
            s3 = res_row0[2];
            s4 = res_row0[3];
            res_row0[0] = res_row3[0];
            res_row0[1] = res_row3[1];
            res_row0[2] = res_row3[2];
            res_row0[3] = res_row3[3];
            res_row3[0] = s1;
            res_row3[1] = s2;
            res_row3[2] = s3;
            res_row3[3] = s4;
            
        }
        
        
        /*if column 1 was not pivoted then do Step 1 in Gaussian Elimination
         
                row 1 math
         
                Divide first row by A_row0[0] to make A_row0[0] == 1
         */
        
        div = A_row0[0];
        A_row0[0] /= div;
        A_row0[1] /= div;
        A_row0[2] /= div;
        A_row0[3] /= div;
        res_row0[0] = res_row0[0]/div;
        res_row0[1] = res_row0[1]/div;
        res_row0[2] = res_row0[2]/div;
        res_row0[3] = res_row0[3]/div;
        
        // row 2 math
        negX = -A_row1[0];
        A_row1[0] += A_row0[0]*negX;
        A_row1[1] += A_row0[1]*negX;
        A_row1[2] += A_row0[2]*negX;
        A_row1[3] += A_row0[3]*negX;
        res_row1[0] += res_row0[0]*negX;
        res_row1[1] += res_row0[1]*negX;
        res_row1[2] += res_row0[2]*negX;
        res_row1[3] += res_row0[3]*negX;
        
        
        // row 3 math
        negX = -A_row2[0];
        A_row2[0] += negX;
        A_row2[1] += A_row0[1]*negX;
        A_row2[2] += A_row0[2]*negX;
        A_row2[3] += A_row0[3]*negX;
        res_row2[0] += res_row0[0]*negX;
        res_row2[1] += res_row0[1]*negX;
        res_row2[2] += res_row0[2]*negX;
        res_row2[3] += res_row0[3]*negX;
        
        
        // row 4 math
        negX = -A_row3[0];
        A_row3[0] += negX;
        A_row3[1] += A_row0[1]*negX;
        A_row3[2] += A_row0[2]*negX;
        A_row3[3] += A_row0[3]*negX;
        res_row3[0] += res_row0[0]*negX;
        res_row3[1] += res_row0[1]*negX;
        res_row3[2] += res_row0[2]*negX;
        res_row3[3] += res_row0[3]*negX;
        
        
        /*======================== Step 2 of Gaussian Elimination ========*/
        // Pivot Point Test for Column 2 excluding row 1
        r2fab = Math.abs(A_row1[1]);
        r3fab = Math.abs(A_row2[1]);
        r4fab = Math.abs(A_row3[1]);
        
        if( r3fab > r2fab ) {
            s1 = A_row1[0];
            s2 = A_row1[1];
            s3 = A_row1[2];
            s4 = A_row1[3];
            A_row1[0] = A_row2[0];
            A_row1[1] = A_row2[1];
            A_row1[2] = A_row2[2];
            A_row1[3] = A_row2[3];
            A_row2[0] = s1;
            A_row2[1] = s2;
            A_row2[2] = s3;
            A_row2[3] = s4;
            
            s1 = res_row1[0];
            s2 = res_row1[1];
            s3 = res_row1[2];
            s4 = res_row1[3];
            res_row1[0] = res_row2[0];
            res_row1[1] = res_row2[1];
            res_row1[2] = res_row2[2];
            res_row1[3] = res_row2[3];
            res_row2[0] = s1;
            res_row2[1] = s2;
            res_row2[2] = s3;
            res_row2[3] = s4;
            
        }
        if( r4fab > r2fab ) {
            s1 = A_row1[0];
            s2 = A_row1[1];
            s3 = A_row1[2];
            s4 = A_row1[3];
            A_row1[0] = A_row3[0];
            A_row1[1] = A_row3[1];
            A_row1[2] = A_row3[2];
            A_row1[3] = A_row3[3];
            A_row3[0] = s1;
            A_row3[1] = s2;
            A_row3[2] = s3;
            A_row3[3] = s4;
            
            s1 = res_row1[0];
            s2 = res_row1[1];
            s3 = res_row1[2];
            s4 = res_row1[3];
            res_row1[0] = res_row3[0];
            res_row1[1] = res_row3[1];
            res_row1[2] = res_row3[2];
            res_row1[3] = res_row3[3];
            res_row3[0] = s1;
            res_row3[1] = s2;
            res_row3[2] = s3;
            res_row3[3] = s4;
            
        }
        
        /* row 2 math
        Divide row 2 by A_row1[1] to make A_row1[1] == 1
         */
        div = A_row1[1];
        A_row1[1] /= div;
        A_row1[2] /= div;
        A_row1[3] /= div;
        res_row1[0] /= div;
        res_row1[1] /= div;
        res_row1[2] /= div;
        res_row1[3] /= div;
        
        // row 1 math
        negX = -A_row0[1];
        A_row0[1] += negX;
        A_row0[2] += A_row1[2]*negX;
        A_row0[3] += A_row1[3]*negX;
        res_row0[0] += negX*res_row1[0];
        res_row0[1] += negX*res_row1[1];
        res_row0[2] += negX*res_row1[2];
        res_row0[3] += negX*res_row1[3];
        
        
        // row 3 math
        negX = -A_row2[1];
        A_row2[1] += negX;
        A_row2[2] += A_row1[2]*negX;
        A_row2[3] += A_row1[3]*negX;
        res_row2[0] += negX*res_row1[0];
        res_row2[1] += negX*res_row1[1];
        res_row2[2] += negX*res_row1[2];
        res_row2[3] += negX*res_row1[3];
        
        
        // row 4 math
        negX = -A_row3[1];
        A_row3[1] += negX;
        A_row3[2] += A_row1[2]*negX;
        A_row3[3] += A_row1[3]*negX;
        res_row3[0] += negX*res_row1[0];
        res_row3[1] += negX*res_row1[1];
        res_row3[2] += negX*res_row1[2];
        res_row3[3] += negX*res_row1[3];
        
        
        /*====================== Step 3 of Gaussian Elimination ================================*/
        
        r3fab = Math.abs(A_row2[2]);
        r4fab = Math.abs(A_row3[2]);
        
        // Pivot Point Test for Column 3 exluding row 1 and 2
        
        
        if( r4fab > r3fab ) {
            s1 = A_row2[0];
            s2 = A_row2[1];
            s3 = A_row2[2];
            s4 = A_row2[3];
            A_row2[0] = A_row3[0];
            A_row2[1] = A_row3[1];
            A_row2[2] = A_row3[2];
            A_row2[3] = A_row3[3];
            A_row3[0] = s1;
            A_row3[1] = s2;
            A_row3[2] = s3;
            A_row3[3] = s4;
            
            s1 = res_row2[0];
            s2 = res_row2[1];
            s3 = res_row2[2];
            s4 = res_row2[3];
            res_row2[0] = res_row3[0];
            res_row2[1] = res_row3[1];
            res_row2[2] = res_row3[2];
            res_row2[3] = res_row3[3];
            res_row3[0] = s1;
            res_row3[1] = s2;
            res_row3[2] = s3;
            res_row3[3] = s4;
            
        }
        // row 3 math
        div = A_row2[2];
        A_row2[2] /= div;
        A_row2[3] /= div;
        res_row2[0] /= div;
        res_row2[1] /= div;
        res_row2[2] /= div;
        res_row2[3] /= div;
        
        //row 1 math
        negX = -A_row0[2];
        A_row0[2] += negX;
        A_row0[3] += A_row2[3]*negX;
        res_row0[0] += negX*res_row2[0];
        res_row0[1] += negX*res_row2[1];
        res_row0[2] += negX*res_row2[2];
        res_row0[3] += negX*res_row2[3];
        
        
        // row 2 math
        negX = -A_row1[2];
        A_row1[2] += negX;
        A_row1[3] += A_row2[3]*negX;
        res_row1[0] += negX*res_row2[0];
        res_row1[1] += negX*res_row2[1];
        res_row1[2] += negX*res_row2[2];
        res_row1[3] += negX*res_row2[3];
        
        // row 4 math
        negX = -A_row3[2];
        A_row3[2] += negX;
        A_row3[3] += A_row2[3]*negX;
        res_row3[0] += negX*res_row2[0];
        res_row3[1] += negX*res_row2[1];
        res_row3[2] += negX*res_row2[2];
        res_row3[3] += negX*res_row2[3];
        
        
        
        /*======================= Step 4 of Gaussian Elimination ===============================*/
        
        // No Pivot Point Test since we are at row 4
        
        // row 4 math
        
        div = A_row3[3];
        
        A_row3[3] /= div;
        res_row3[0] /= div;
        res_row3[1] /= div;
        res_row3[2] /= div;
        res_row3[3] /= div;
        
        // row 1 math
        negX = -A_row0[3];
        A_row0[3] += negX;
        res_row0[0] += negX*res_row3[0];
        res_row0[1] += negX*res_row3[1];
        res_row0[2] += negX*res_row3[2];
        res_row0[3] += negX*res_row3[3];
        
        
        // row 2 math
        negX = -A_row1[3];
        A_row1[3] = negX + A_row1[3];
        res_row1[0] += negX*res_row3[0];
        res_row1[1] += negX*res_row3[1];
        res_row1[2] += negX*res_row3[2];
        res_row1[3] += negX*res_row3[3];
        
        
        // row 3 math
        negX = -A_row2[3];
        A_row2[3] = negX + A_row2[3];
        res_row2[0] += negX*res_row3[0];
        res_row2[1] += negX*res_row3[1];
        res_row2[2] += negX*res_row3[2];
        res_row2[3] += negX*res_row3[3];
        
        return res;
    }
    
    
    /**
     * Removes a specified number of rows from a matrix of doubles.
     * @param mat The matrix that will be shortened.
     * @param nbrRowsToRemove The number of rows to be removed from the end of mat.
     * @return The shortened matrix of double.
     */
    public final static double[][] removeRows(double[][] mat, int nbrRowsToRemove) {
        if (nbrRowsToRemove <= 0)
            return (double[][])(mat.clone());
        
        final int nbrRows = mat.length - nbrRowsToRemove;
        final int nbrCols = mat[0].length;
        double [][] shortenedMatrix = new double [nbrRows][nbrCols];
        for (int i = 0; i < nbrRows; i++) {
            for (int j = 0; j < nbrCols; j++)
                shortenedMatrix[i][j] = mat[i][j];
        }
        return shortenedMatrix;
    }
    
    /**
     * Prints matrix mat to the standard output.
     * @param mat The matrix.
     */
    public static void printMatrix(double[][] mat) {
        for (int i = 0; i < mat.length; i++) {
            for (int j = 0; j < mat[0].length; j++)
                System.out.print(mat[i][j] + " ");
            System.out.println();
        }
        System.out.println();
    }
    
    /**
     * Prints vector v to the standard output.
     * @param v The vector.
     */
    public static void printVector(double[] v) {
        for (int i = 0; i < v.length; i++) {
            System.out.println(v[i]);
        }
        System.out.println();
    }
    
    /**
     * A few tests.
     */
    /*
     public static void main(String[]args){
        double[][] mat1 = {{1, 2, 3}, {4, 5, 6}};
        double[][] mat2 = {{1, 2}, {3, 4}, {5, 6}};
        double[] diag = {1, 2, 3};
        
        double[][] res = MatrixUtils.matrix_x_diagonalMatrix(mat1, diag);
        printMatrix(res);
        
        res = MatrixUtils.matrixTrans_x_diagonalMatrix(mat2, diag);
        printMatrix(res);
        
        res = MatrixUtils.ATPA(mat2, diag);
        printMatrix(res);
        
        Matrix mat_res = MatrixUtils.ATA(new Matrix(mat2));
        mat_res.print(15, 1);
        
        double[][] a = {{1, 2, 3}};
        double[][] b = {{1, 2}};
        res = matrixATrans_x_matrixB(a, b);
        printMatrix(res);
    }*/
}
