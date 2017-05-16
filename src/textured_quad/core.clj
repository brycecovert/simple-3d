(ns textured-quad.core
  
  (:import [com.badlogic.gdx ApplicationAdapter Gdx ApplicationListener]
           [com.badlogic.gdx.graphics GL20 GL30 Texture Mesh VertexAttribute VertexAttributes$Usage Color PerspectiveCamera]
           [com.badlogic.gdx.graphics.g3d Model ModelBatch ModelInstance Material Environment]
           [com.badlogic.gdx.graphics.g3d.attributes ColorAttribute TextureAttribute]
           [com.badlogic.gdx.graphics.g3d.utils ModelBuilder ]
           [com.badlogic.gdx.graphics.glutils ShaderProgram]
           [com.badlogic.gdx.backends.lwjgl LwjglApplication]
           [com.badlogic.gdx.math Vector2 Intersector Vector3 Matrix4]
           
           )
  (:gen-class))

(defn make-application [] 
  (let [mesh (atom nil)
        model-instances (atom nil)
        model-batch (atom nil)
        tex (atom nil)
        cam (atom nil)
        environment (atom nil)]
    (reify 
      ApplicationListener
      (create [this]
        (reset! tex (Texture. (.internal Gdx/files "brick.png")))
        (let [cam (reset! cam (doto (PerspectiveCamera. 48.262 320 240)
                                (.rotate (Vector3. 1 0 0) 85.8)
                                (#(.set (.position %) 0 0 0.41))
                                (#(set! (.near %) 0.01))))
              _ (.update cam)
              box (.createBox (ModelBuilder.)
                              1 1 0.6519
                              (Material. (into-array [(TextureAttribute/createDiffuse @tex)]))
                              (bit-or VertexAttributes$Usage/Position VertexAttributes$Usage/Normal VertexAttributes$Usage/TextureCoordinates))]
          (reset! environment (doto (Environment.)
                                (.set (ColorAttribute. ColorAttribute/AmbientLight 0.8 0.8 0.8 1.0))))

          ;; todo change this to map
          (reset! model-instances [(ModelInstance. box -1.0 1.0 0.35)
                                   (ModelInstance. box 1.0 1.0 0.35)
                                   (ModelInstance. box -1.0 2.0 0.35)
                                   (ModelInstance. box 0.0 -2.0 0.35)
                                   (ModelInstance. box 0.0 2.0 0.35)
                                   (ModelInstance. box -1.0 -2.0 0.35)

                                   (ModelInstance. box -6.0 -4.0 0.35)
                                   (ModelInstance. box -5.0 -4.0 0.35)
                                   (ModelInstance. box -4.0 -3.0 0.35)
                                   (ModelInstance. box -4.0 -2.0 0.35)
                                   (ModelInstance. box -4.0 -1.0 0.35)
                                   (ModelInstance. box -4.0 0.0 0.35)
                                   (ModelInstance. box -4.0 1.0 0.35)
                                   (ModelInstance. box 1.0 -2.0 0.35)
                                   (ModelInstance. box 2.0 -2.0 0.35)
                                   (ModelInstance. box 2.0 -1.0 0.35)
                                   (ModelInstance. box 2.0 0.0 0.35)])
          (reset! model-batch (ModelBatch.))
          ) 
        nil)
      (dispose [this] nil)
      (pause [this] nil)
      (resume [this] nil)
      (render [this]
        (.glClearColor Gdx/gl 0 0 0 1)
        (.glClear Gdx/gl (bit-or GL30/GL_COLOR_BUFFER_BIT GL30/GL_DEPTH_BUFFER_BIT))
        (.rotateAround @cam Vector3/Zero (Vector3. 0 0 1) 0.5)
        (.update @cam)
        (.begin @model-batch @cam)
        (doseq [m @model-instances]
          (.render @model-batch m @environment))
        (.end @model-batch))
      (resize [this width height]
        nil))))


(defn -main []
  (LwjglApplication. (make-application) "test" 640 480))
