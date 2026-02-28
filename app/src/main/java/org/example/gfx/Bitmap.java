package org.example.gfx;

public class Bitmap {
    public int width;
    public int height;
    public int[] pixels;

    public Bitmap(int width, int height) {
        this.width = width;
        this.height = height;
        this.pixels = new int[width * height];
    }

    public void render(Bitmap b, int ox, int oy) {
        // 1. 画像(b)の「上から下へ」1行ずつ順番に見ていきます
        for (int y = 0; y < b.height; y++) {
            
            // 今見ている画像の縦の位置(y)に、表示したい場所(oy)を足して、
            // 画面上の「本当の縦の位置」を計算します
            int yy = y + oy;

            // もし画面の外（上すぎ、または下すぎ）にはみ出していたら、
            // その行は描く必要がないので、次の行の処理へスキップします
            if (yy < 0 || yy >= height) {
                continue;
            }

            // 2. その行の中で「左から右へ」1ピクセルずつ順番に見ていきます
            for (int x = 0; x < b.width; x++) {
                
                // 今見ている画像の横の位置(x)に、表示したい場所(ox)を足して、
                // 画面上の「本当の横の位置」を計算します
                int xx = x + ox;

                // もし画面の外（左すぎ、または右すぎ）にはみ出していたら、
                // そのピクセルは描かずに次のピクセルへスキップします
                if (xx < 0 || xx >= width) {
                    continue;
                }

                // 3. 元の画像(b)から「色（データ）」を取り出します
                // 画像は1列のデータとして保存されているので [横 + 縦 * 幅] で場所を特定します
                int alpha = b.pixels[x + y * b.width];

                // 4. もしその色が「透明（0）」でなければ、画面に色を塗ります
                if (alpha > 0) {
                    // 画面(pixels)の指定した場所(xx, yy)に、色を書き込みます
                    pixels[xx + yy * width] = alpha;
                }
            }
        }
    }
}
