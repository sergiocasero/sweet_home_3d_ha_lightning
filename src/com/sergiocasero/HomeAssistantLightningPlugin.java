
package com.sergiocasero;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Predicate;

import javax.imageio.ImageIO;
import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.eteks.sweethome3d.j3d.AbstractPhotoRenderer;
import com.eteks.sweethome3d.j3d.AbstractPhotoRenderer.Quality;
import com.eteks.sweethome3d.j3d.PhotoRenderer;
import com.eteks.sweethome3d.model.Camera;
import com.eteks.sweethome3d.model.Home;
import com.eteks.sweethome3d.model.HomeLight;
import com.eteks.sweethome3d.model.HomePieceOfFurniture;
import com.eteks.sweethome3d.model.PieceOfFurniture;
import com.eteks.sweethome3d.plugin.Plugin;
import com.eteks.sweethome3d.plugin.PluginAction;
import com.eteks.sweethome3d.viewcontroller.AbstractPhotoController;


public class HomeAssistantLightningPlugin extends Plugin {
    @Override
    public PluginAction[] getActions() {
        return new PluginAction [] {new HomeAssistantLightningAction()};
    }

    public class HomeAssistantLightningAction extends PluginAction {
    	
    	private Home home;
    	
        @Override
        public void execute() {
        	// Get the current home, we'll do changes on it per iteration
        	home = getHome();
        	HomeAssistantLightningOptions options = createInputPanel();
        	System.out.println(options);
            List<HomePieceOfFurniture> lights = getAllHomeLights();
            
            if (createInfoPanel(lights)) {
    			try {
    				List<List<Float>> combinations = generateYaml(options.getPath(), lights);
    	            generateImages(options, combinations, lights);
    			} catch (IOException e) {
    				// TODO Auto-generated catch block
    				e.printStackTrace();
    			}
            }
            
        }
        
        public boolean createInfoPanel(List<HomePieceOfFurniture> lights) {
        	String lightString = "";
        	
        	for (HomePieceOfFurniture light : lights) {
				lightString += light.getName() + "\r\n";
			}
        	
            
            return JOptionPane.showConfirmDialog(null, 
            		"I've found " + lights.size() + " lights: \r\n" 
			            + lightString 
			            + "So i'll generate " + Math.pow(2, lights.size() )
			            	+ " images.\r\n"
			            	+ "Is that OK?"
			            ) == JOptionPane.OK_OPTION;
        }

		public HomeAssistantLightningOptions createInputPanel() {
		  JTextField pathField = new JTextField(5);
	      JTextField widthField = new JTextField(5);
	      JTextField heightField = new JTextField(5);
	      JTextField qualityField = new JTextField(5);
	      
	      
	      JPanel inputPanel = new JPanel();
	      inputPanel.add(new JLabel("Output path:"));
	      inputPanel.add(pathField);
	      inputPanel.add(Box.createVerticalStrut(15)); // a spacer
	      inputPanel.add(new JLabel("Image width:"));
	      inputPanel.add(widthField);
	      inputPanel.add(Box.createVerticalStrut(15)); // a spacer
	      inputPanel.add(new JLabel("Image height:"));
	      inputPanel.add(heightField);
	      inputPanel.add(Box.createVerticalStrut(15)); // a spacer
	      inputPanel.add(new JLabel("Quality (high or low):"));
	      inputPanel.add(qualityField);

	      int result = JOptionPane.showConfirmDialog(null, inputPanel, 
	               "Please fill the values and press ok", JOptionPane.OK_CANCEL_OPTION);
	      if (result != JOptionPane.OK_OPTION) {
	    	  // TODO: Throw exception or something
	      }
	      
	      return new HomeAssistantLightningOptions(
	    		  pathField.getText(),
	    		  Integer.parseInt(widthField.getText()),
	    		  Integer.parseInt(heightField.getText()),
	    		  qualityField.getText().equals("high") ? Quality.HIGH : Quality.LOW
	    	);
	   }
    	
        public HomeAssistantLightningAction() {
           putPropertyValue(Property.NAME, "Home Assistant Lightning");
           putPropertyValue(Property.MENU, "Tools");
           
           // Enables the action by default
           setEnabled(true);
        }
        
        private List<List<Float>>  generateYaml(String path, List<HomePieceOfFurniture> lights) throws IOException {
        	List<List<Float>> results = new ArrayList<>();
            List<Float> in1 = new ArrayList<>();
            List<Float> in2 = new ArrayList<>();
            
            for (int i = 0;i < 10 ; i++) {
                in1.add(0.5f);
                in2.add(0.0f);
            } 
            
            recursivelyCombine(results, new ArrayList<Float>(), in1, in2, 0);
            
            String yaml = "type: picture-elements\n"
            		+ "image: /local/planes/casa_noche.jpg\n"
            		+ "elements:\n";
            
            for (List<Float> list : results) {
            	String image = "";

            	yaml += "  - conditions:\r\n";
            	
            	for (int i = 0; i < list.size(); i++) {
            		String state = list.get(i) == 0.0 ? "off" : "on";
                	image += state;

                	yaml += "      - entity: " + lights.get(i).getName() + "\r\n"
                			+ "        state: '" + state + "'\r\n";
				}
            	

            	yaml += "    elements:\r\n"
    			+ "      - entity:\r\n";
            	
            	for (int i = 0; i < list.size(); i++) {
            		yaml += "          - " + lights.get(i).getName() + "\r\n";
				}

    			yaml += "        filter: brightness(100%)\r\n"
    			+ "        image: /local/planes/" + image + ".jpg\r\n"
    			+ "        style:\r\n"
    			+ "          left: 50%\r\n"
    			+ "          top: 50%\r\n"
    			+ "          width: 100%\r\n"
    			+ "        type: image\r\n"
    			+ "    type: conditional\r\n";



    		}
            

			FileWriter outputfile = new FileWriter(path + "/config.yaml");
			outputfile.write(yaml);
			outputfile.close();
			
			return results;
        }
        
        private void generateImages(
        		HomeAssistantLightningOptions options, 
        		List<List<Float>> combinations, 
        		List<HomePieceOfFurniture> lights
        ) {
        	for (List<Float> list : combinations) {
            	String name = "";
            	
            	ArrayList<HomeLight> currentLights = new ArrayList<>();
            	
            	for (int i = 0; i < list.size(); i++) {
                	name += list.get(i) == 0.0 ? "off" : "on";
                	
                	HomeLight light = (HomeLight) lights.get(i);
                	light.setPitch(list.get(i));
                	light.setPower(list.get(i));
                	currentLights.add(light);
				}
            	System.out.println("-----------------------------------");
            	
            	ArrayList<HomePieceOfFurniture> itemsToRemove = new ArrayList<>();
            	
            	for (int i = 0; i < home.getFurniture().size(); i++) {
					if(home.getFurniture().get(i) instanceof HomeLight) {
						itemsToRemove.add(home.getFurniture().get(i));
					}
				}
            	
            	for (int i = 0; i < itemsToRemove.size(); i++) {
					home.deletePieceOfFurniture(itemsToRemove.get(i));
				}
            	
            	for (int i = 0; i < currentLights.size(); i++) {
					home.addPieceOfFurniture(currentLights.get(i));
				}
            	
            	createImage(home, options, name);
            	
    		}
            
            JOptionPane.showMessageDialog(null, "Done!");
        }
        
        private void createImage(Home home, HomeAssistantLightningOptions options, String name) {
        	try {
        		System.out.println("Generating image " + options.getPath() + "/" + name);
        		long millis = System.currentTimeMillis();
            	
        		PhotoRenderer renderer  = new PhotoRenderer(home, options.getQuality());
        		
    			BufferedImage image = new BufferedImage(options.getImageWidth(), options.getImageHeight(), BufferedImage.TYPE_INT_RGB);
    			renderer.render(image, home.getCamera(), new ImageObserver() {
    				
    				@Override
    				public boolean imageUpdate(Image img, int infoflags, int x, int y, int width, int height) {
    					System.out.print(".");
    					return false;
    				}
    			});

    			File outputfile = new File(options.getPath() + "/" + name + ".jpg");
    			ImageIO.write(image, "jpg", outputfile);
    			
    			System.out.println("Image generated, Time needed:" + (System.currentTimeMillis() - millis));


    		} catch (IOException e) {
    			e.printStackTrace();
    		}
        }
        
        public List<HomePieceOfFurniture> getAllHomeLights() {
        	List<HomePieceOfFurniture> lights = new ArrayList<>();
            
            for (int i = 0; i < home.getFurniture().size(); i++) {
            	if (home.getFurniture().get(i) instanceof HomeLight) {
            		lights.add(home.getFurniture().get(i));
            	}
				
			}
            
            return lights;
        }
    }
    
    
    
    void recursivelyCombine(List<List<Float>> result, List<Float> current, List<Float> in1, List<Float> in2, int index) {
        if (index == in1.size()) {
            result.add(current);
        } else {
            if (in1.get(index).equals(in2.get(index))) {
               current.add(in1.get(index));
               recursivelyCombine(result, current, in1, in2, index+1);
            } else {
               List<Float> temp = new ArrayList<>(current);
               temp.add(0.5f);
               recursivelyCombine(result, temp, in1, in2, index+1);

               temp = new ArrayList<>(current);
               temp.add(0.0f);
               recursivelyCombine(result, temp, in1, in2, index+1);
            }
        }
    }

}
