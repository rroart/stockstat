import numpy as np
import torch
import pickle as pkl

def real_loss(predicted_outputs, loss_fn, device):
    batch_size = predicted_outputs.shape[0]
    targets = torch.ones(batch_size).to(device)
    real_loss = loss_fn(predicted_outputs.squeeze(), targets)
    
    return real_loss


def fake_loss(predicted_outputs, loss_fn, device):
    batch_size = predicted_outputs.shape[0]
    targets = torch.zeros(batch_size).to(device)
    fake_loss = loss_fn(predicted_outputs.squeeze(), targets)
    
    return fake_loss

# Training loop function

def train_mnist_gan(discriminator, generator, discriminator_optim, generator_optim, loss_fn, dl, n_epochs, device, verbose=False):
    print(f'Training on [{device}]...')
    
    z_size = 100
    fixed_z = np.random.uniform(-1, 1, size=(16, z_size))
    fixed_z = torch.from_numpy(fixed_z).float().to(device)          
    fixed_samples = []
    discriminator_losses = []
    generator_losses = []
    
    
    # Move discriminator and generator to available device
    discriminator = discriminator.to(device)
    generator = generator.to(device)
    
    for epoch in range(n_epochs):
        print(f'Epoch [{epoch+1}/{n_epochs}]:')
        discriminator.train()
        generator.train()
        discriminator_running_batch_loss = 0
        generator_running_batch_loss = 0
        for curr_batch, (real_images, _) in enumerate(dl):
            real_images = real_images.to(device)
            
            discriminator_optim.zero_grad()
            
            real_images = (real_images * 2) - 1
            discriminator_real_logits_out = discriminator(real_images)
            discriminator_real_loss = real_loss(discriminator_real_logits_out, loss_fn, device)
            #discriminator_real_loss = real_loss(discriminator_real_logits_out, smooth=True)
            
            with torch.no_grad():
                z = np.random.uniform(-1, 1, size=(dl.batch_size, z_size))
                z = torch.from_numpy(z).float().to(device)
                fake_images = generator(z)
            discriminator_fake_logits_out = discriminator(fake_images)
            discriminator_fake_loss = fake_loss(discriminator_fake_logits_out, loss_fn, device)
            #discriminator_fake_loss = fake_loss(discriminator_fake_logits_out)
            discriminator_loss = discriminator_real_loss + discriminator_fake_loss
            discriminator_loss.backward()
            discriminator_optim.step()
            # Save discriminator batch loss
            discriminator_running_batch_loss += discriminator_loss
            
            generator_optim.zero_grad()
            
            #z = torch.rand(size=(dl.batch_size, z_size)).to(device)
            z = np.random.uniform(-1, 1, size=(dl.batch_size, z_size))
            z = torch.from_numpy(z).float().to(device)       
            fake_images = generator(z)
            generator_logits_out = discriminator(fake_images)
            generator_loss = real_loss(generator_logits_out, loss_fn, device)
            #generator_loss = real_loss(generator_logits_out)
            generator_loss.backward()
            generator_optim.step()
            generator_running_batch_loss += generator_loss
            
            if curr_batch % 400 == 0 and verbose:
                print(f'\tBatch [{curr_batch:>4}/{len(dl):>4}] - d_batch_loss: {discriminator_loss.item():.6f}\tg_batch_loss: {generator_loss.item():.6f}')
            
        discriminator_epoch_loss = discriminator_running_batch_loss.item()/len(dl)
        generator_epoch_loss = generator_running_batch_loss.item()/len(dl)
        discriminator_losses.append(discriminator_epoch_loss)
        generator_losses.append(generator_epoch_loss)
        
        print(f'epoch_d_loss: {discriminator_epoch_loss:.6f} \tepoch_g_loss: {generator_epoch_loss:.6f}')
        
        generator.eval()
        fixed_samples.append(generator(fixed_z).detach().cpu())
        
    # Finally write generated fake images from fixed latent vector to disk
    with open('mnist.pkl', 'wb') as f:
        pkl.dump(fixed_samples, f)
     
    return discriminator_losses, generator_losses
