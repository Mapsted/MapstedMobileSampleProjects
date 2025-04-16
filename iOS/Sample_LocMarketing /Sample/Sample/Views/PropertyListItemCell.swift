//
//  PropertyListItemCell.swift
//  MapstedComponentsUI
//
//  Created by Parth Bhatt on 03/07/23.
//

import UIKit
import MapstedCore
import SDWebImage

class PropertyListItemCell: UITableViewCell {
    
    @IBOutlet weak var lblPropertyName: UILabel!
    @IBOutlet weak var lblPropertyAddress: UILabel!
    @IBOutlet weak var imgVProperty: UIImageView!
    @IBOutlet weak var containerView: UIView!
    @IBOutlet weak var bottomContainerView: UIView!
    
    override func awakeFromNib() {
        super.awakeFromNib()
    
        // Initial setup for rounded corners
        configureViewCorners()
    }
    
    override func layoutSubviews() {
        super.layoutSubviews()
        
        configureViewCorners()
    }
    
    // Apply rounded corners
    private func configureViewCorners() {
        // Configure the corner radius only when needed
        containerView.layer.cornerRadius = 8.0
        containerView.layer.masksToBounds = true
        
    }
    
    override func setSelected(_ selected: Bool, animated: Bool) {
        super.setSelected(selected, animated: animated)
    }
    
    
    func fetchImage(imageBaseUrl: String?, imageUid: String?) {
        self.imgVProperty.contentMode = .scaleToFill
        guard let imageBaseUrl = imageBaseUrl, let imageUid = imageUid else {
            let errorImage = UIImage.loadFromModule(imageName: "image_load_error")
            self.imgVProperty.image = errorImage
            self.imgVProperty.backgroundColor = .white
            return
        }
        
        let imageUrl = URL(string: imageBaseUrl + imageUid)
        
        self.imgVProperty.sd_imageIndicator = SDWebImageActivityIndicator.gray
        self.imgVProperty.sd_setImage(
            with: imageUrl,
            placeholderImage: nil,  // You can add a placeholder image if needed
            options: [],
            context: nil,
            progress: nil,
            completed: { [weak self] image, error, cacheType, imageURL in
                guard let self = self else { return }
                if let image = image, error == nil {
                    self.imgVProperty.image = image
                } else {
                    let errorImage = UIImage.loadFromModule(imageName: "image_load_failed")
                    self.imgVProperty.image = errorImage
                    self.imgVProperty.contentMode = .center
                    self.imgVProperty.backgroundColor = .white
                }
            }
        )
        
    }
}
