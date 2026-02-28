package org.example.gfx;

public class Bitmap3D extends Bitmap {
    // fov（視野角）: 画面の高さ(height)を基準にして、カメラの広角具合を決めます
    private double fov = height;
    
    // depthBuffer（デプスバッファ）: 画面の各ピクセルが「カメラからどれくらい遠いか」を記録する専用のメモ帳
    private double[] depthBuffer;

    public Bitmap3D(int width, int height) {
        super(width, height);
        // 画面のピクセル数と同じサイズのメモ帳を用意します
        depthBuffer = new double[width * height];
    }

    int t;           // 時間（アニメーション用）
    double xCam = 0; // カメラの世界地図上でのX座標（横）
    double yCam = 0; // カメラの世界地図上でのY座標（縦）
    double zCam = 3; // カメラの高さ
    double rot = 0;  // カメラの向いている角度

    public void render() {
        t++;

        // --- 1. 回転の準備 ---
        // カメラの角度(rot)から、縦横をどれくらい混ぜ合わせるかの「調合レシピ」を作ります
        double rSin = Math.sin(rot);
        double rCos = Math.cos(rot);

        // --- 2. 画面を上から下へ（y軸）スキャン ---
        for (int y = 0; y < height; y++) {
            
            // yd: 画面の真ん中を 0 として、今塗ろうとしている行が上下にどれだけズレているか
            double yd = (y - (height / 2)) / fov;

            // 【距離(zd)の逆算】
            // 画面の中央(ydが小さい)に近いほど、遠くの地面を指しているはずです。
            // なので、高さを「画面のズレ(yd)」で割り算して、地面までの距離を割り出します。
            double zd = (6 + zCam) / yd;
            
            // もし画面の上半分（地平線より上）なら、空（天井）として距離を計算し直します
            if (yd < 0) {
                zd = (6 - zCam) / -yd;
            }

            // --- 3. その行を左から右へ（x軸）スキャン ---
            for (int x = 0; x < width; x++) {
                
                // xd: 画面の真ん中を 0 として、左右にどれだけズレているか（視線の角度）
                double xd = (x - (width / 2)) / fov;
                
                // 【遠近法の適用（懐中電灯の魔法）】
                // 遠く（zdが大きい）の行を描くときほど、横方向の「歩幅」を大きくして、
                // 広い範囲の地面をギュッと画面の1ピクセルに縮小して詰め込みます。
                xd *= zd;

                // 【回転行列による座標変換】
                // 画面上の相対的な位置(xd, zd)を、実際の地図上の「どの方角か」に変換します。
                // さきほど作った調合レシピ（rCos, rSin）を使って縦横の座標を混ぜ合わせます。
                double xx = (xd * rCos - zd * rSin + xCam);
                double yy = (xd * rSin + zd * rCos + yCam);

                // 計算で出た小数点付きの地図座標を、整数（タイルのマス目）に直します
                int xPix = (int) xx;
                int yPix = (int) yy;

                // 【マイナス座標のズレ補正】
                // コンピュータは -0.5 を (int) にすると 0 にしてしまいます（本当は -1 のマスに入ってほしい）。
                // そのため、0未満の場合は強制的に -1 して、タイルのマス目がズレないようにします。
                if (xx < 0) {
                    xPix--;
                }
                if (yy < 0) {
                    yPix--;
                }

                // 後で「霧」をかけるために、このピクセルの「距離(zd)」をメモ帳に書き込んでおきます
                depthBuffer[x + y * width] = zd;

                // --- 4. 描画色の決定 ---
                // 地図のマス目(xPix, yPix)から、テクスチャ画像の色を拾ってきます。
                // (xPix & 15) は「0〜15の数字を無限に繰り返す」という魔法のビット演算で、
                // これにより16x16ピクセルの画像が、床にタイル状に敷き詰められます。
                pixels[x + y * width] = Textures.floors.pixels[(xPix & 15) | (yPix & 15) * Textures.floors.width];
                
                // さらに、特定の場所(pX, pY)にだけ、別のブロックや装飾を上書きして描きます
                drawFloor(xd, zd, yd, x, y, xPix, yPix, 0, 2);
                drawFloor(xd, zd, yd, x, y, xPix, yPix, 0, 3);
                drawFloor(xd, zd, yd, x, y, xPix, yPix, 1, 2);
                drawFloor(xd, zd, yd, x, y, xPix, yPix, 1, 3);
                
                drawCeiling(xd, zd, yd, x, y, xPix, yPix, 0, 2);
                drawCeiling(xd, zd, yd, x, y, xPix, yPix, 0, 3);
                drawCeiling(xd, zd, yd, x, y, xPix, yPix, 1, 2);
                drawCeiling(xd, zd, yd, x, y, xPix, yPix, 1, 3);
            }
        }
    }

    // --- 霧（フォグ）を描画する処理 ---
    public void renderFog() {
        // 画面の全ピクセルを1つずつチェックします
        for (int i = 0; i < depthBuffer.length; i++) {
            int color = pixels[i]; // 画面に塗られている今の色（例: 0xRRGGBB）
            
            // 【色の分解】
            // 1つの数字にまとまっている色データを「赤・緑・青」の3つに分解します。
            // >> はデータを右にズラす操作、& 0xff は下8桁だけを取り出す操作です。
            int r = (color >> 16) & 0xff; // 赤を取り出す
            int g = (color >> 8) & 0xff;  // 緑を取り出す
            int b = (color) & 0xff;       // 青を取り出す
            
            // 【暗さの計算】
            // メモ帳(depthBuffer)に記録した距離を見て、遠いほど数字が小さくなる（暗くなる）ようにします。
            double brightness = 255 - depthBuffer[i] * 2;
            
            // 【色を暗くする】
            // 取り出した赤・緑・青に明るさを掛け算して、暗い色を作ります。
            r = (int) (r / 255.0 * brightness);
            g = (int) (g / 255.0 * brightness);
            b = (int) (b / 255.0 * brightness);
            
            // 【色の組み立て】
            // 暗くした赤・緑・青を、<<（左にズラす）を使って再び1つの色データに合体させ、画面に戻します。
            pixels[i] = r << 16 | g << 8 | b;
        }
    }

    // --- 特定の場所に床の装飾を描く処理 ---
    public void drawFloor(double xd, double zd, double yd, int x, int y, int xPix, int yPix, int pX, int pY) {
        // もし「画面の下半分（yd >= 0）」かつ「指定された 16x16 のマス目の範囲内」であれば...
        if (yd >= 0  && xd >= pX * 16 && xd < pX * 16 + 16 && zd >= pY * 16 && zd < pY * 16 + 16) {
            // ベースの床とは違う場所（+16ズラした場所）のテクスチャ画像を上書きで塗ります
            pixels[x + y * width] = Textures.floors.pixels[(xPix & 15) + 16 | (yPix & 15) * Textures.floors.width];
        }
    }

    // --- 特定の場所に天井の装飾を描く処理 ---
    public void drawCeiling(double xd, double zd, double yd, int x, int y, int xPix, int yPix, int pX, int pY) {
        // もし「画面の上半分（yd <= 0）」かつ「指定された 16x16 のマス目の範囲内」であれば...
        if (yd <= 0 && xd >= pX * 16 && xd < pX * 16 + 16 && zd >= pY * 16 && zd < pY * 16 + 16) {
            // 天井用のテクスチャ画像を上書きで塗ります
            pixels[x + y * width] = Textures.floors.pixels[(xPix & 15) + 16 | (yPix & 15) * Textures.floors.width];
        }
    }
}
